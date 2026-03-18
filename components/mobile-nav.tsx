'use client';

import { LayoutDashboard, Package, Settings } from 'lucide-react';
import { Button } from '@/components/ui/button';
import { cn } from '@/lib/utils';

interface MobileNavProps {
  activeTab?: 'dashboard' | 'orders' | 'settings';
  onTabChange?: (tab: 'dashboard' | 'orders' | 'settings') => void;
}

const navItems = [
  { id: 'dashboard', label: 'Dashboard', icon: LayoutDashboard },
  { id: 'orders', label: 'Orders', icon: Package },
  { id: 'settings', label: 'Settings', icon: Settings },
];

export function MobileNav({ activeTab = 'dashboard', onTabChange }: MobileNavProps) {
  return (
    <nav className="md:hidden fixed bottom-0 left-0 right-0 z-40 bg-card border-t border-border/50 backdrop-blur-sm">
      <div className="flex items-center justify-around">
        {navItems.map((item) => {
          const Icon = item.icon;
          const isActive = activeTab === item.id;

          return (
            <Button
              key={item.id}
              variant="ghost"
              onClick={() => onTabChange?.(item.id as 'dashboard' | 'orders' | 'settings')}
              className={cn(
                'flex-1 h-16 flex flex-col items-center justify-center gap-1 rounded-none transition-colors duration-200',
                isActive
                  ? 'text-primary bg-secondary/50'
                  : 'text-muted-foreground hover:text-foreground'
              )}
            >
              <Icon className="w-5 h-5" />
              <span className="text-xs font-medium">{item.label}</span>
            </Button>
          );
        })}
      </div>
    </nav>
  );
}
