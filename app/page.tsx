'use client';

import { useState } from 'react';
import { Header } from '@/components/header';
import { Sidebar } from '@/components/sidebar';
import { MobileNav } from '@/components/mobile-nav';
import { DeliveryCard, type DeliveryCardProps } from '@/components/delivery-card';
import { Button } from '@/components/ui/button';
import { Mail } from 'lucide-react';

// Mock delivery data
const mockDeliveries: DeliveryCardProps[] = [
  {
    id: '1',
    productName: 'Wireless Noise-Cancelling Headphones Pro Max',
    platform: 'amazon',
    status: 'delivered',
    expectedDate: 'Mar 15, 2026',
  },
  {
    id: '2',
    productName: 'Premium Cotton T-Shirt Pack (3)',
    platform: 'myntra',
    status: 'shipped',
    expectedDate: 'Mar 19, 2026',
  },
  {
    id: '3',
    productName: 'Summer Fashion Casual Sneakers',
    platform: 'flipkart',
    status: 'out-for-delivery',
    expectedDate: 'Mar 18, 2026',
  },
  {
    id: '4',
    productName: 'Portable USB-C Fast Charger 65W',
    platform: 'amazon',
    status: 'order-placed',
    expectedDate: 'Mar 22, 2026',
  },
  {
    id: '5',
    productName: 'Stainless Steel Water Bottle 1L',
    platform: 'flipkart',
    status: 'shipped',
    expectedDate: 'Mar 20, 2026',
  },
  {
    id: '6',
    productName: 'Designer Sunglasses with UV Protection',
    platform: 'myntra',
    status: 'delivered',
    expectedDate: 'Mar 16, 2026',
  },
];

export default function Home() {
  const [activeTab, setActiveTab] = useState<'dashboard' | 'orders' | 'settings'>('dashboard');
  const [searchQuery, setSearchQuery] = useState('');

  const filteredDeliveries = mockDeliveries.filter((delivery) =>
    delivery.productName.toLowerCase().includes(searchQuery.toLowerCase())
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
                {filteredDeliveries.length > 0 ? (
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
                          : 'Connect your Gmail to start tracking'}
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
