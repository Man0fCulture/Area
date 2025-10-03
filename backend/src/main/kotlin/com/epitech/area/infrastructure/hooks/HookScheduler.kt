package com.epitech.area.infrastructure.hooks

import com.epitech.area.domain.entities.TriggerType
import com.epitech.area.domain.repositories.AreaRepository
import com.epitech.area.domain.repositories.ServiceRepository
import kotlinx.coroutines.*
import org.slf4j.LoggerFactory
import java.util.concurrent.ConcurrentHashMap

class HookScheduler(
    private val areaRepository: AreaRepository,
    private val serviceRepository: ServiceRepository,
    private val hookProcessor: HookProcessor
) {
    private val logger = LoggerFactory.getLogger(HookScheduler::class.java)
    private val scheduledJobs = ConcurrentHashMap<String, Job>()
    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    
    private var pollingJob: Job? = null
    private val pollingInterval = 30_000L

    fun start() {
        logger.info("Starting hook scheduler...")
        
        pollingJob = scope.launch {
            while (isActive) {
                try {
                    processPollingHooks()
                } catch (e: Exception) {
                    logger.error("Error processing polling hooks", e)
                }
                delay(pollingInterval)
            }
        }
        
        logger.info("Hook scheduler started with ${pollingInterval}ms interval")
    }

    fun stop() {
        logger.info("Stopping hook scheduler...")
        pollingJob?.cancel()
        scheduledJobs.values.forEach { it.cancel() }
        scheduledJobs.clear()
        scope.cancel()
        logger.info("Hook scheduler stopped")
    }

    private suspend fun processPollingHooks() {
        logger.debug("Checking for polling hooks...")
        
        val activeAreas = areaRepository.findActiveAreas()
        logger.debug("Found ${activeAreas.size} active areas")

        for (area in activeAreas) {
            try {
                val service = serviceRepository.findById(area.action.serviceId)
                if (service == null) {
                    logger.warn("Service not found for area ${area.id}")
                    continue
                }

                val actionDef = service.actions.find { it.id == area.action.actionId }
                if (actionDef == null) {
                    logger.warn("Action ${area.action.actionId} not found in service ${service.name}")
                    continue
                }

                when (actionDef.triggerType) {
                    TriggerType.POLLING -> {
                        logger.debug("Processing polling action for area ${area.id}")
                        processPollingArea(area)
                    }
                    TriggerType.SCHEDULE -> {
                        logger.debug("Processing schedule action for area ${area.id}")
                        processScheduleArea(area)
                    }
                    TriggerType.WEBHOOK -> {
                    }
                    TriggerType.EVENT -> {
                    }
                }
            } catch (e: Exception) {
                logger.error("Error processing area ${area.id}", e)
            }
        }
    }

    private suspend fun processPollingArea(area: com.epitech.area.domain.entities.Area) {
        scope.launch {
            try {
                val shouldTrigger = hookProcessor.checkTriggerCondition(area)
                
                if (shouldTrigger) {
                    logger.info("Trigger condition met for area ${area.id}, executing...")
                    val result = hookProcessor.processArea(area)
                    
                    if (result.success) {
                        logger.info("Area ${area.id} executed successfully")
                    } else {
                        logger.warn("Area ${area.id} execution failed: ${result.error}")
                    }
                }
            } catch (e: Exception) {
                logger.error("Error processing polling area ${area.id}", e)
            }
        }
    }

    private suspend fun processScheduleArea(area: com.epitech.area.domain.entities.Area) {
        val jobKey = "schedule_${area.id}"
        
        if (scheduledJobs.containsKey(jobKey)) {
            return
        }

        when (area.action.actionId) {
            "every_x_seconds" -> {
                val interval = area.action.config["interval"]?.toLongOrNull() ?: return
                val job = scope.launch {
                    while (isActive) {
                        delay(interval * 1000)
                        try {
                            logger.info("Timer triggered for area ${area.id} (every ${interval}s)")
                            hookProcessor.processArea(area)
                        } catch (e: Exception) {
                            logger.error("Error executing scheduled area ${area.id}", e)
                        }
                    }
                }
                scheduledJobs[jobKey] = job
            }
            "at_time" -> {
                val targetTime = area.action.config["time"] ?: return
                val job = scope.launch {
                    while (isActive) {
                        val now = System.currentTimeMillis()
                        val nextTrigger = calculateNextTriggerTime(targetTime)
                        val delayMs = nextTrigger - now
                        
                        if (delayMs > 0) {
                            delay(delayMs)
                            try {
                                logger.info("Timer triggered for area ${area.id} at $targetTime")
                                hookProcessor.processArea(area)
                            } catch (e: Exception) {
                                logger.error("Error executing scheduled area ${area.id}", e)
                            }
                        }
                        
                        delay(60_000)
                    }
                }
                scheduledJobs[jobKey] = job
            }
        }
    }

    private fun calculateNextTriggerTime(time: String): Long {
        val parts = time.split(":")
        if (parts.size != 2) return System.currentTimeMillis()
        
        val hour = parts[0].toIntOrNull() ?: return System.currentTimeMillis()
        val minute = parts[1].toIntOrNull() ?: return System.currentTimeMillis()
        
        val now = java.util.Calendar.getInstance()
        val target = java.util.Calendar.getInstance().apply {
            set(java.util.Calendar.HOUR_OF_DAY, hour)
            set(java.util.Calendar.MINUTE, minute)
            set(java.util.Calendar.SECOND, 0)
            set(java.util.Calendar.MILLISECOND, 0)
        }
        
        if (target.before(now)) {
            target.add(java.util.Calendar.DAY_OF_MONTH, 1)
        }
        
        return target.timeInMillis
    }

    fun cancelScheduledArea(areaId: String) {
        val jobKey = "schedule_$areaId"
        scheduledJobs[jobKey]?.cancel()
        scheduledJobs.remove(jobKey)
        logger.info("Cancelled scheduled jobs for area $areaId")
    }

    suspend fun triggerAreaManually(areaId: org.bson.types.ObjectId) {
        val area = areaRepository.findById(areaId)
        if (area == null) {
            logger.warn("Area $areaId not found for manual trigger")
            return
        }
        
        logger.info("Manually triggering area ${area.id}")
        scope.launch {
            hookProcessor.processArea(area)
        }
    }
}
