'use client';

import { LayoutDashboard, Package, Settings } from 'lucide-react';
import { Button } from '@/components/ui/button';
import { cn } from '@/lib/utils';

interface SidebarProps {
  activeTab?: 'dashboard' | 'orders' | 'settings';
  onTabChange?: (tab: 'dashboard' | 'orders' | 'settings') => void;
}

const navItems = [
  { id: 'dashboard', label: 'Dashboard', icon: LayoutDashboard },
  { id: 'orders', label: 'Orders', icon: Package },
  { id: 'settings', label: 'Settings', icon: Settings },
];

export function Sidebar({ activeTab = 'dashboard', onTabChange }: SidebarProps) {
  return (
    <aside className="hidden md:flex fixed left-0 top-16 h-[calc(100vh-4rem)] w-20 bg-sidebar border-r border-sidebar-border flex-col items-center gap-3 py-6">
      {navItems.map((item) => {
        const Icon = item.icon;
        const isActive = activeTab === item.id;

        return (
          <Button
            key={item.id}
            variant="ghost"
            size="icon"
            onClick={() => onTabChange?.(item.id as 'dashboard' | 'orders' | 'settings')}
            className={cn(
              'w-10 h-10 rounded-md transition-all duration-200',
              isActive
                ? 'bg-sidebar-primary text-sidebar-primary-foreground'
                : 'text-sidebar-foreground hover:bg-secondary'
            )}
            title={item.label}
          >
            <Icon className="w-4 h-4" />
          </Button>
        );
      })}
    </aside>
  );
}
