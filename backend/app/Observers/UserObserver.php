<?php

namespace App\Observers;

use App\Models\User;

class UserObserver
{
    public function created(User $user): void
    {
        // Cash wallet singleton
        $user->wallets()->create([
            'name'    => 'Cash',
            'type'    => 'cash',
            'color'   => '#4CAF50',
            'icon'    => 'wallet',
            'balance' => 0,
        ]);

        // Default categories
        $categories = [
            ['name' => 'Gaji',      'type' => 'income',  'color' => '#4CAF50', 'icon' => 'salary'],
            ['name' => 'Bisnis',    'type' => 'income',  'color' => '#2196F3', 'icon' => 'business'],
            ['name' => 'Makan',     'type' => 'expense', 'color' => '#FF5722', 'icon' => 'restaurant'],
            ['name' => 'Transport', 'type' => 'expense', 'color' => '#FF9800', 'icon' => 'directions_car'],
            ['name' => 'Belanja',   'type' => 'expense', 'color' => '#E91E63', 'icon' => 'shopping_cart'],
            ['name' => 'Hiburan',   'type' => 'expense', 'color' => '#9C27B0', 'icon' => 'movie'],
            ['name' => 'Kesehatan', 'type' => 'expense', 'color' => '#00BCD4', 'icon' => 'local_hospital'],
            ['name' => 'Tagihan',   'type' => 'expense', 'color' => '#607D8B', 'icon' => 'receipt'],
        ];

        foreach ($categories as $category) {
            $user->categories()->create($category);
        }
    }
}
