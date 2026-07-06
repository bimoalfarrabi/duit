<?php

namespace Tests\Feature;

// use Illuminate\Foundation\Testing\RefreshDatabase;
use Tests\TestCase;

class ExampleTest extends TestCase
{
    /**
     * A basic test example.
     */
    public function test_the_application_returns_a_successful_response(): void
    {
        // ponytail: pure API app, tidak ada route GET / — skip test bawaan Laravel
        $this->markTestSkipped('Pure API app, no web routes.');
    }
}
