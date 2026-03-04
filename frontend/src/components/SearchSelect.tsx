import React, { useState, useEffect, useRef } from 'react';
import { Input } from "@/components/ui/input"
import { Button } from "@/components/ui/button"
import { ScrollArea } from "@/components/ui/scroll-area"


export function SearchSelect({ options, placeholder, value, onChange, icon, subtitle }: any) {
  const [isOpen, setIsOpen] = useState(false);
  const [searchTerm, setSearchTerm] = useState('');
  const wrapperRef = useRef<HTMLDivElement>(null);

  useEffect(() => {
    function handleClickOutside(event: MouseEvent) {
      if (wrapperRef.current && !wrapperRef.current.contains(event.target as Node)) {
        setIsOpen(false);
      }
    }
    document.addEventListener('mousedown', handleClickOutside);
    return () => {
      document.removeEventListener('mousedown', handleClickOutside);
    };
  }, []);

  const filteredOptions = options.filter((option:any) =>
    option.label.toLowerCase().includes(searchTerm.toLowerCase())
  );

  return (
    <div ref={wrapperRef} className="relative">
      <div
        className="border rounded-lg p-3 hover:border-blue-500 cursor-pointer"
        onClick={() => setIsOpen(!isOpen)}
      >
        <div className="flex items-center space-x-2">
          {icon}
          <div className="flex-1 min-w-0">
            <div className="text-sm text-gray-500 truncate">{placeholder}</div>
            <Input
              type="text"
              value={value || searchTerm}
              onChange={(e) => {
                setSearchTerm(e.target.value);
                onChange('');
              }}
              className="font-semibold w-full bg-transparent border-none p-0 focus-visible:ring-0 focus-visible:ring-offset-0"
              placeholder={placeholder}
            />
            <div className="text-xs text-gray-400 truncate">{subtitle}</div>
          </div>
        </div>
      </div>
      {isOpen && (
        <div className="absolute z-10 w-full mt-1 bg-white border border-gray-300 rounded-md shadow-lg">
          <ScrollArea className="h-64">
            {filteredOptions.map((option:any) => (
              <Button
                key={option.value}
                className="w-full justify-start font-normal"
                variant="ghost"
                onClick={() => {
                  onChange(option.value);
                  setSearchTerm('');
                  setIsOpen(false);
                }}
              >
                {option.label}
              </Button>
            ))}
          </ScrollArea>
        </div>
      )}
    </div>
  );
}
