'use client';

import { Search, LogOut, User } from 'lucide-react';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Avatar, AvatarFallback, AvatarImage } from '@/components/ui/avatar';

interface HeaderProps {
  onSearch?: (query: string) => void;
  onLogout?: () => void;
}

export function Header({ onSearch, onLogout }: HeaderProps) {
  return (
    <header className="sticky top-0 z-40 w-full bg-background/95 backdrop-blur-sm border-b border-border/50">
      <div className="h-16 px-4 sm:px-6 flex items-center justify-between gap-2">
        {/* Left: App name */}
        <div className="flex items-center gap-2 min-w-0">
          <div className="w-7 h-7 sm:w-8 sm:h-8 rounded-md bg-primary/80 flex-shrink-0 flex items-center justify-center">
            <span className="text-primary-foreground font-bold text-xs">DT</span>
          </div>
          <h1 className="text-sm sm:text-base font-semibold text-foreground tracking-tight whitespace-nowrap">
            Delivery Tracker
          </h1>
        </div>

        {/* Right: Search, Avatar, Logout */}
        <div className="flex items-center gap-2 sm:gap-3">
          {/* Search bar - Hidden on mobile */}
          <div className="hidden sm:flex items-center gap-2 bg-input border border-border rounded-md px-3 py-1.5 w-48 md:w-56">
            <Search className="w-4 h-4 text-muted-foreground flex-shrink-0" />
            <Input
              type="text"
              placeholder="Search orders..."
              className="border-0 bg-transparent text-xs sm:text-sm focus:outline-none focus-visible:ring-0 placeholder-muted-foreground"
              onChange={(e) => onSearch?.(e.target.value)}
            />
          </div>

          {/* User Avatar */}
          <Avatar className="w-7 h-7 sm:w-8 sm:h-8 cursor-pointer hover:opacity-80 transition-opacity flex-shrink-0">
            <AvatarImage src="https://avatar.vercel.sh/user" />
            <AvatarFallback className="bg-secondary text-xs">
              <User className="w-3.5 h-3.5 sm:w-4 sm:h-4" />
            </AvatarFallback>
          </Avatar>

          {/* Logout button */}
          <Button
            variant="ghost"
            size="sm"
            onClick={onLogout}
            className="text-muted-foreground hover:text-foreground hover:bg-secondary h-7 w-7 sm:h-8 sm:w-8 p-0 flex-shrink-0"
          >
            <LogOut className="w-3.5 h-3.5 sm:w-4 sm:h-4" />
          </Button>
        </div>
      </div>
    </header>
  );
}
