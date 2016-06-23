namespace pwm {

constexpr int value_range = 1000;

void init();

void set(int value);

bool is_running_on_dc();

// Freq in mHz
int get_mains_freq();

}

