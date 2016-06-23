#include <Arduino.h>

namespace slots {

constexpr int max_client_name = 30;
constexpr int max_client_count = 10;

void update(String client_name, int value, int deadline_ms);

String dump_state();

int get_current_setting();

}
