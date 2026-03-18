'use client';

import { useEffect, useMemo, useState } from 'react';
import { Header } from '@/components/header';
import { Sidebar } from '@/components/sidebar';
import { MobileNav } from '@/components/mobile-nav';
import { DeliveryCard, type DeliveryCardProps } from '@/components/delivery-card';
import { Button } from '@/components/ui/button';
import { Mail } from 'lucide-react';
import { fetchOrders, type ApiOrder } from '@/lib/api/orders';

const SUPPORTED_PLATFORMS = ['amazon', 'flipkart', 'myntra'] as const;

function isSupportedPlatform(platform: string): platform is DeliveryCardProps['platform'] {
  return (SUPPORTED_PLATFORMS as readonly string[]).includes(platform);
}

function mapOrderStatus(status: string): DeliveryCardProps['status'] {
  const normalized = status.trim().toUpperCase();

  if (normalized === 'DELIVERED') {
    return 'delivered';
  }
  if (normalized === 'OUT_FOR_DELIVERY') {
    return 'out-for-delivery';
  }
  if (normalized === 'SHIPPED') {
    return 'shipped';
  }
  if (normalized === 'DELAYED') {
    return 'delayed';
  }
  if (normalized === 'CANCELLED') {
    return 'cancelled';
  }

  return 'order-placed';
}

function mapApiOrderToDeliveryCard(order: ApiOrder): DeliveryCardProps {
  const normalizedPlatform = order.platform.trim().toLowerCase();
  const platform = isSupportedPlatform(normalizedPlatform) ? normalizedPlatform : 'amazon';

  const expectedDate = new Date(order.deliveryDate).toLocaleDateString('en-US', {
    month: 'short',
    day: '2-digit',
    year: 'numeric',
  });

  return {
    id: String(order.id),
    platform,
    productName: order.productName,
    status: mapOrderStatus(order.status),
    expectedDate,
  };
}

export default function Home() {
  const [activeTab, setActiveTab] = useState<'dashboard' | 'orders' | 'settings'>('dashboard');
  const [searchQuery, setSearchQuery] = useState('');
  const [deliveries, setDeliveries] = useState<DeliveryCardProps[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const loadOrders = async () => {
    try {
      setIsLoading(true);
      setError(null);
      const apiOrders = await fetchOrders();
      setDeliveries(apiOrders.map(mapApiOrderToDeliveryCard));
    } catch {
      setError('Could not load orders from backend. Please try again.');
    } finally {
      setIsLoading(false);
    }
  };

  useEffect(() => {
    loadOrders();
  }, []);

  const filteredDeliveries = useMemo(
    () =>
      deliveries.filter((delivery) =>
        delivery.productName.toLowerCase().includes(searchQuery.toLowerCase())
      ),
    [deliveries, searchQuery]
  );

  const handleLogout = () => {
    // Handle logout logic here
    console.log('Logout clicked');
  };

  const handleConnectGmail = () => {
    // Handle Gmail connection logic here
    console.log('Connect Gmail clicked');
  };

  return (
    <div className="min-h-screen bg-background">
      {/* Header */}
      <Header onSearch={setSearchQuery} onLogout={handleLogout} />

      <div className="flex">
        {/* Sidebar - Desktop only */}
        <Sidebar activeTab={activeTab} onTabChange={setActiveTab} />

        {/* Main Content */}
        <main className="flex-1 md:ml-20 pt-16 pb-32 md:pb-24">
          {activeTab === 'dashboard' && (
            <div className="max-w-5xl mx-auto px-4 sm:px-6 md:px-12">
              {/* Dashboard Header */}
              <div className="mb-12 sm:mb-16 md:mb-20">
                <h1 className="text-3xl sm:text-4xl md:text-5xl lg:text-6xl font-bold text-foreground mb-3 sm:mb-6 leading-tight tracking-tight">
                  Your Deliveries
                </h1>
                <p className="text-sm sm:text-base text-muted-foreground max-w-2xl">
                  Track all your orders from multiple platforms in one unified view
                </p>
              </div>

              {/* Connect Gmail CTA */}
              <div className="mb-16 sm:mb-20 md:mb-24">
                <Button
                  onClick={handleConnectGmail}
                  className={`
                    px-3 sm:px-4 py-2 text-xs sm:text-sm font-medium
                    bg-primary text-primary-foreground
                    rounded-sm transition-colors duration-200
                    hover:bg-primary/85
                  `}
                >
                  <Mail className="w-3 h-3 sm:w-3.5 sm:h-3.5 mr-2" />
                  Connect Gmail
                </Button>
              </div>

              {/* Delivery Cards Grid */}
              <div>
                {isLoading ? (
                  <div className="flex flex-col items-center justify-center py-24 sm:py-32">
                    <p className="text-sm sm:text-base text-muted-foreground">Loading orders...</p>
                  </div>
                ) : error ? (
                  <div className="flex flex-col items-center justify-center py-24 sm:py-32">
                    <div className="text-center px-4">
                      <h3 className="text-sm sm:text-base font-medium text-foreground mb-2 sm:mb-3">
                        Unable to load dashboard
                      </h3>
                      <p className="text-xs sm:text-sm text-muted-foreground mb-4">{error}</p>
                      <Button onClick={loadOrders} className="rounded-sm">
                        Retry
                      </Button>
                    </div>
                  </div>
                ) : filteredDeliveries.length > 0 ? (
                  <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-4 sm:gap-5 md:gap-6">
                    {filteredDeliveries.map((delivery) => (
                      <DeliveryCard
                        key={delivery.id}
                        {...delivery}
                      />
                    ))}
                  </div>
                ) : (
                  <div className="flex flex-col items-center justify-center py-24 sm:py-32">
                    <div className="text-center px-4">
                      <div className="inline-flex items-center justify-center w-10 sm:w-12 h-10 sm:h-12 rounded-sm bg-secondary mb-4 sm:mb-6">
                        <Mail className="w-5 sm:w-6 h-5 sm:h-6 text-muted-foreground" />
                      </div>
                      <h3 className="text-sm sm:text-base font-medium text-foreground mb-2 sm:mb-3">
                        No deliveries found
                      </h3>
                      <p className="text-xs sm:text-sm text-muted-foreground">
                        {searchQuery
                          ? 'Try adjusting your search'
                          : 'Orders will appear here once backend data is available'}
                      </p>
                    </div>
                  </div>
                )}
              </div>
            </div>
          )}

          {activeTab === 'orders' && (
            <div className="max-w-5xl mx-auto px-4 sm:px-6 md:px-12">
              <h1 className="text-3xl sm:text-4xl md:text-5xl font-bold text-foreground mb-4 sm:mb-6">Orders</h1>
              <p className="text-sm sm:text-base text-muted-foreground">View all your orders and their history</p>
            </div>
          )}

          {activeTab === 'settings' && (
            <div className="max-w-5xl mx-auto px-4 sm:px-6 md:px-12">
              <h1 className="text-3xl sm:text-4xl md:text-5xl font-bold text-foreground mb-4 sm:mb-6">Settings</h1>
              <p className="text-sm sm:text-base text-muted-foreground">Manage your preferences and account</p>
            </div>
          )}
        </main>
      </div>

      {/* Mobile Navigation - Mobile only */}
      <MobileNav activeTab={activeTab} onTabChange={setActiveTab} />
    </div>
  );
}
