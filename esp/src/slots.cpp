#include "slots.h"
#include <string.h>

namespace slots {
namespace {

struct Slot {
  bool used;
  char client_name[max_client_name+1];
  int value;
  long deadline;
};

Slot slots[max_client_count];

void kill_old_slots() {
  long now = millis();
  for (int i=0; i<max_client_count; i++) {
    if (slots[i].deadline < now) {
      slots[i].used = false;
    }
  }
}

}  // namespace anonymous

void update(String client_name, int value, int deadline_ms) {
  char normalized_name[max_client_name+1];
  memset(normalized_name, 0, sizeof(normalized_name));
  strcpy(normalized_name, client_name.substring(0, max_client_name).c_str());
  //strcpyn(normalized_name, client_name.c_str(), max_client_name);

  int found_idx = -1;
  for (int i=0; i<max_client_count; i++) {
    if (memcmp(normalized_name, slots[i].client_name, max_client_name)==0) {
      found_idx = i;
      break;
    }
  }

  for (int i=0; (found_idx<0) && (i<max_client_count); i++) {
    if (!slots[i].used) {
      found_idx = i;
    }
  }

  if (found_idx >= 0) {
    memcpy(slots[found_idx].client_name, normalized_name, max_client_name);
    slots[found_idx].used = true;
    slots[found_idx].value = value;
    slots[found_idx].deadline = millis() + deadline_ms;
  }
  kill_old_slots();
}

String dump_state() {
  long now = millis();
  String res;
  for (int i=0; i<max_client_count; i++) {
    res += i; res += "> ";
    if (slots[i].used) {
      long left_ms = slots[i].deadline - now;
      res += "val="; res += slots[i].value;
      res += ", left_ms="; res += left_ms;
      res += ", client_name="; res += slots[i].client_name;
    }
    res += "\n";
  }
  return res;
}

int get_current_setting() {
  kill_old_slots();
  int val = 0;
  for (int i=0; i<max_client_count; i++) {
    if (slots[i].used) {
      if (slots[i].value > val) {
        val = slots[i].value;
      }
    }
  }
  return val;
}

}  // namespace slots
