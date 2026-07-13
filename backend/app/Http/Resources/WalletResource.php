<?php

namespace App\Http\Resources;

use Illuminate\Http\Request;
use Illuminate\Http\Resources\Json\JsonResource;

class WalletResource extends JsonResource
{
    public function toArray(Request $request): array
    {
        return [
            'id'         => $this->id,
            'user_id'    => $this->user_id,
            'name'       => $this->name,
            'type'       => $this->type,
            'color'      => $this->color,
            'icon'       => $this->icon,
            'balance'    => $this->balance,
            'is_owner'   => $request->user() !== null && $this->user_id === $request->user()->id,
            'is_shared'  => $this->when(
                $request->user() !== null && $this->user_id === $request->user()->id,
                fn () => $this->members()->exists()
            ),
        ];
    }
}
