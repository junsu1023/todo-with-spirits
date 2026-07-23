import { ChevronsUpDown } from 'lucide-react'
import { useState } from 'react'
import { Popover, PopoverContent, PopoverTrigger } from './popover'

interface DropdownSelectProps {
  options: string[]
  value: string
  onChange: (value: string) => void
  className?: string
}

export function DropdownSelect({
  options,
  value,
  onChange,
  className,
}: DropdownSelectProps) {
  const [open, setOpen] = useState(false)

  return (
    <Popover open={open} onOpenChange={setOpen}>
      <PopoverTrigger
        className={`flex items-center gap-1 text-sm text-gray-500 hover:text-gray-700 ${className ?? ''}`}
      >
        <span>{value}</span>
        <ChevronsUpDown size={13} className="text-gray-400" />
      </PopoverTrigger>
      <PopoverContent
        side="bottom"
        align="end"
        className="w-32 p-0 overflow-hidden"
      >
        {options.map((option, i) => (
          <div key={option}>
            {i > 0 && <div className="h-px bg-gray-100" />}
            <button
              type="button"
              onClick={() => {
                onChange(option)
                setOpen(false)
              }}
              className={`w-full px-4 py-2.5 text-center text-sm transition-colors hover:bg-gray-50 ${
                option === value
                  ? 'text-[#B286FD] font-medium'
                  : 'text-gray-500'
              }`}
            >
              {option}
            </button>
          </div>
        ))}
      </PopoverContent>
    </Popover>
  )
}
