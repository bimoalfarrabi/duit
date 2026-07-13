<?php

namespace App\Http\Resources;

use Illuminate\Http\Request;
use Illuminate\Http\Resources\Json\JsonResource;

class WalletInvitationResource extends JsonResource
{
    public function toArray(Request $request): array
    {
        return [
            'id'          => $this->id,
            'wallet_id'   => $this->wallet_id,
            'wallet_name' => $this->whenLoaded('wallet', fn () => $this->wallet->name),
            'inviter'     => $this->whenLoaded('inviter', fn () => [
                'id'   => $this->inviter->id,
                'name' => $this->inviter->name,
            ]),
            'email'       => $this->email,
            'status'      => $this->status,
            'expires_at'  => $this->expires_at?->toIso8601String(),
        ];
    }
}
