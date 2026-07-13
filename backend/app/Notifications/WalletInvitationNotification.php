<?php

namespace App\Notifications;

use App\Models\WalletInvitation;
use Illuminate\Bus\Queueable;
use Illuminate\Notifications\Messages\MailMessage;
use Illuminate\Notifications\Notification;

class WalletInvitationNotification extends Notification
{
    use Queueable;

    public function __construct(
        private readonly WalletInvitation $invitation,
    ) {}

    /** @return array<int, string> */
    public function via(object $notifiable): array
    {
        return ['mail'];
    }

    public function toMail(object $notifiable): MailMessage
    {
        $wallet  = $this->invitation->wallet;
        $inviter = $this->invitation->inviter;
        $url     = rtrim(config('app.frontend_url', config('app.url')), '/')
            . '/invitations/' . $this->invitation->token;

        return (new MailMessage)
            ->subject('Undangan wallet bersama di Duit')
            ->greeting('Halo!')
            ->line("{$inviter->name} mengundangmu untuk mengakses wallet \"{$wallet->name}\" di Duit.")
            ->action('Lihat Undangan', $url)
            ->line('Undangan ini berlaku sampai ' . $this->invitation->expires_at->format('d M Y H:i') . '.')
            ->line('Abaikan email ini jika kamu tidak mengenal pengirim.');
    }
}
