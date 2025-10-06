import React from 'react';
import { cn } from '@/utils/cn';

interface InputProps extends React.InputHTMLAttributes<HTMLInputElement> {
  label?: string;
  error?: string;
  helperText?: string;
}

export const Input = React.forwardRef<HTMLInputElement, InputProps>(
  ({ label, error, helperText, className, ...props }, ref) => {
    return (
      <div className="w-full">
        {label && (
          <label className="block text-sm font-medium text-[#2C3E50] mb-1.5">
            {label}
            {props.required && <span className="text-[#E74C3C] ml-1">*</span>}
          </label>
        )}
        <input
          ref={ref}
          className={cn(
            'w-full px-3 py-2 border rounded-lg text-[#2C3E50] placeholder:text-[#95A5A6]',
            'focus:outline-none focus:ring-2 focus:ring-[#FF6B35] focus:border-transparent',
            'disabled:bg-gray-50 disabled:cursor-not-allowed',
            error ? 'border-[#E74C3C]' : 'border-gray-200',
            className
          )}
          {...props}
        />
        {error && <p className="mt-1 text-sm text-[#E74C3C]">{error}</p>}
        {helperText && !error && <p className="mt-1 text-sm text-[#95A5A6]">{helperText}</p>}
      </div>
    );
  }
);

Input.displayName = 'Input';
