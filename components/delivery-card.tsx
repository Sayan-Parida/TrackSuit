'use client';

import { Badge } from '@/components/ui/badge';
import { Card } from '@/components/ui/card';
import { Package, Truck, CheckCircle, Clock, AlertCircle, XCircle } from 'lucide-react';

export interface DeliveryCardProps {
  id: string;
  productName: string;
  platform: 'amazon' | 'flipkart' | 'myntra';
  status: 'delivered' | 'shipped' | 'out-for-delivery' | 'order-placed' | 'delayed' | 'cancelled';
  expectedDate: string;
}

const platformIcons: Record<string, JSX.Element> = {
  amazon: (
    <div className="w-8 h-8 bg-[#FF9900] rounded-full flex items-center justify-center text-white font-bold text-sm">
      A
    </div>
  ),
  flipkart: (
    <div className="w-8 h-8 bg-[#1F40FB] rounded-full flex items-center justify-center text-white font-bold text-sm">
      F
    </div>
  ),
  myntra: (
    <div className="w-8 h-8 bg-[#F4A460] rounded-full flex items-center justify-center text-white font-bold text-sm">
      M
    </div>
  ),
};

const statusConfig: Record<DeliveryCardProps['status'], {
  label: string;
  icon: JSX.Element;
}> = {
  delivered: {
    label: 'Delivered',
    icon: <CheckCircle className="w-3 h-3" />,
  },
  shipped: {
    label: 'Shipped',
    icon: <Package className="w-3 h-3" />,
  },
  'out-for-delivery': {
    label: 'Out for Delivery',
    icon: <Truck className="w-3 h-3" />,
  },
  'order-placed': {
    label: 'Order Placed',
    icon: <Clock className="w-3 h-3" />,
  },
  delayed: {
    label: 'Delayed',
    icon: <AlertCircle className="w-3 h-3" />,
  },
  cancelled: {
    label: 'Cancelled',
    icon: <XCircle className="w-3 h-3" />,
  },
};

export function DeliveryCard({
  productName,
  platform,
  status,
  expectedDate,
}: DeliveryCardProps) {
  const statusInfo = statusConfig[status];

  return (
    <Card
      className={`
        bg-card border border-border
        rounded-sm p-3 sm:p-4 transition-colors duration-200
        hover:border-border/70
      `}
    >
      <div className="space-y-2.5 sm:space-y-3">
        {/* Header */}
        <div className="flex items-start justify-between gap-2 sm:gap-3">
          <div className="flex items-center gap-1.5 sm:gap-2 min-w-0">
            {platformIcons[platform]}
            <span className="text-xs font-medium text-muted-foreground capitalize truncate">
              {platform}
            </span>
          </div>
          <div className="flex items-center gap-0.5 sm:gap-1 text-xs text-muted-foreground whitespace-nowrap flex-shrink-0">
            {statusInfo.icon}
            <span className="hidden sm:inline">{statusInfo.label}</span>
            <span className="sm:hidden">{statusInfo.label.split(' ')[0]}</span>
          </div>
        </div>

        {/* Product name */}
        <div>
          <h2 className="text-xs sm:text-sm font-medium text-foreground leading-snug line-clamp-2">
            {productName}
          </h2>
        </div>

        {/* Expected delivery date */}
        <div className="text-xs text-muted-foreground">
          Expected: <span className="text-foreground text-xs">{expectedDate}</span>
        </div>

        {/* Progress bar */}
        <div className="w-full h-0.5 bg-secondary rounded-full overflow-hidden">
          <div
            className={`
              h-full transition-all duration-500
              ${
                status === 'delivered'
                  ? 'w-full bg-muted-foreground'
                  : status === 'cancelled'
                    ? 'w-full bg-muted-foreground/60'
                    : status === 'delayed'
                      ? 'w-1/3 bg-muted-foreground/80'
                  : status === 'out-for-delivery'
                    ? 'w-2/3 bg-muted-foreground'
                    : status === 'shipped'
                      ? 'w-1/2 bg-muted-foreground'
                      : 'w-1/4 bg-muted-foreground'
              }
            `}
          />
        </div>
      </div>
    </Card>
  );
}
