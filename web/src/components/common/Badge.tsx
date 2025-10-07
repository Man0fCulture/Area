import React from 'react';
import { cn } from '@/utils/cn';

interface BadgeProps {
  children: React.ReactNode;
  variant?: 'success' | 'error' | 'warning' | 'info' | 'default';
  className?: string;
}

export const Badge: React.FC<BadgeProps> = ({ children, variant = 'default', className }) => {
  const variantClasses = {
    success: 'bg-[#27AE60]/10 text-[#27AE60] border-[#27AE60]/20',
    error: 'bg-[#E74C3C]/10 text-[#E74C3C] border-[#E74C3C]/20',
    warning: 'bg-[#F39C12]/10 text-[#F39C12] border-[#F39C12]/20',
    info: 'bg-[#3498DB]/10 text-[#3498DB] border-[#3498DB]/20',
    default: 'bg-[#95A5A6]/10 text-[#95A5A6] border-[#95A5A6]/20',
  };

  return (
    <span
      className={cn(
        'inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium border',
        variantClasses[variant],
        className
      )}
    >
      {children}
    </span>
  );
};
