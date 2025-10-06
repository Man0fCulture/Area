'use client';

import React from 'react';
import * as Switch from '@radix-ui/react-switch';
import { cn } from '@/utils/cn';

interface ToggleProps {
  checked: boolean;
  onCheckedChange: (checked: boolean) => void;
  disabled?: boolean;
  label?: string;
}

export const Toggle: React.FC<ToggleProps> = ({ checked, onCheckedChange, disabled, label }) => {
  return (
    <div className="flex items-center gap-2">
      <Switch.Root
        checked={checked}
        onCheckedChange={onCheckedChange}
        disabled={disabled}
        className={cn(
          'w-11 h-6 rounded-full relative transition-colors',
          'focus:outline-none focus:ring-2 focus:ring-[#FF6B35] focus:ring-offset-2',
          checked ? 'bg-[#FF6B35]' : 'bg-[#95A5A6]',
          disabled && 'opacity-50 cursor-not-allowed'
        )}
      >
        <Switch.Thumb
          className={cn(
            'block w-5 h-5 bg-white rounded-full transition-transform',
            'shadow-sm',
            checked ? 'translate-x-[22px]' : 'translate-x-[2px]'
          )}
        />
      </Switch.Root>
      {label && <span className="text-sm text-[#2C3E50]">{label}</span>}
    </div>
  );
};
