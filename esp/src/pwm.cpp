#include "pwm.h"
#include <Arduino.h>
#include <stdint.h>

namespace pwm {

namespace {

constexpr int t1_value = 10;
constexpr int t1_frequency = 312500 / t1_value;

// Sync signal input pin.
const int pin_sync = 5;

// Output.
const int pin_out = 4;

uint32_t cnt = 0;
uint32_t sync_start;
uint32_t sync_end;
uint32_t turn_on;
bool last_sync_state = false;

volatile uint32_t period_length;
volatile uint32_t value;

bool is_crossing_zero() {
    return digitalRead(pin_sync);
}

ICACHE_RAM_ATTR
void timer_isr() {
    cnt++;
    bool sync_state = is_crossing_zero();
    if (sync_state != last_sync_state) {
        if (sync_state) {
            // sync start
            digitalWrite(pin_out, 0);
            int32_t sync_length = sync_end-sync_start;
            period_length = cnt - sync_start;
            turn_on = cnt + sync_length / 2 + value * period_length / pwm::value_range;
            sync_start = cnt;
        } else {
            // sync end
            sync_end = cnt;
        }
        last_sync_state = sync_state;
    }
    if (cnt == turn_on) {
        digitalWrite(pin_out, 1);
    }
}

}  // namespace anonymous

void init() {
    value = value_range;
    pinMode(pin_sync, INPUT);
    pinMode(pin_out, OUTPUT);
    timer1_isr_init();
    timer1_attachInterrupt(&timer_isr);
    timer1_write(t1_value);
    timer1_enable(TIM_DIV265, TIM_EDGE, TIM_LOOP);
}

void set(int value_) {
    if (value_ > value_range) {
        value_ = value_range;
    }
    value = value_range - value_;
}

bool is_running_on_dc() {
    bool seen_zero_cross = false;
    unsigned long start_time = millis();
    const unsigned long timeout = 1000;
    do {
        seen_zero_cross = seen_zero_cross || is_crossing_zero();
    } while(millis() - start_time < timeout);
    return !seen_zero_cross;
}

int get_mains_freq() {
    if (period_length < 1) {
        return 0;
    }
    return t1_frequency * 1000 / 2 / period_length;
}

}  // namespace pwm

