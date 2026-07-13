<?php

namespace App\Models;

use Database\Factories\UserFactory;
use Illuminate\Contracts\Auth\MustVerifyEmail;
use Illuminate\Database\Eloquent\Factories\HasFactory;
use Illuminate\Database\Eloquent\Relations\BelongsToMany;
use Illuminate\Database\Eloquent\Relations\HasMany;
use Illuminate\Foundation\Auth\User as Authenticatable;
use Illuminate\Notifications\Notifiable;
use Laravel\Sanctum\HasApiTokens;

class User extends Authenticatable implements MustVerifyEmail
{
    /** @use HasFactory<UserFactory> */
    use HasApiTokens, HasFactory, Notifiable;

    protected $fillable = ['name', 'email', 'password', 'two_factor_secret', 'two_factor_confirmed_at'];

    protected $hidden = ['password', 'remember_token', 'two_factor_secret'];

    protected function casts(): array
    {
        return [
            'email_verified_at'       => 'datetime',
            'two_factor_confirmed_at' => 'datetime',
            'password'                => 'hashed',
        ];
    }

    public function wallets(): HasMany
    {
        return $this->hasMany(Wallet::class);
    }

    public function categories(): HasMany
    {
        return $this->hasMany(Category::class);
    }

    public function transactions(): HasMany
    {
        return $this->hasMany(Transaction::class);
    }

    public function budgets(): HasMany
    {
        return $this->hasMany(Budget::class);
    }

    public function savingsGoals(): HasMany
    {
        return $this->hasMany(SavingsGoal::class);
    }

    /** Wallet milik user lain yang di-share ke user ini. */
    public function sharedWallets(): BelongsToMany
    {
        return $this->belongsToMany(Wallet::class, 'wallet_user')
            ->withPivot('role')
            ->withTimestamps();
    }

    /** ID semua wallet yang bisa diakses: milik sendiri + yang di-share. */
    public function accessibleWalletIds(): array
    {
        return $this->wallets()->pluck('id')
            ->merge($this->sharedWallets()->pluck('wallets.id'))
            ->unique()
            ->values()
            ->all();
    }
}
