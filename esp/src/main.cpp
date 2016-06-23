#include <Arduino.h>
#include <ESP8266WiFi.h>
#include <WiFiClient.h>
#include <ESP8266WebServer.h>
#include <ESP8266mDNS.h>
#include <WiFiManager.h>
#include "pwm.h"
#include "slots.h"

MDNSResponder mdns;
ESP8266WebServer server(80);
String my_hostname;

bool is_configuration_required() {
  return pwm::is_running_on_dc();
}

bool running_configuration = false;

void setup(void){
  my_hostname = "budzik-" + String(ESP.getChipId());

  Serial.begin(115200);
  Serial.println("");
  Serial.println("Hi!");

  pwm::init();
  delay(100);

  WiFiManager wifi_manager;
  if(is_configuration_required()) {
    Serial.println("Configuration requested (or running on DC).");
    delay(500);
    WiFi.enableSTA(true);
    WiFi.disconnect(true);
    Serial.println("Rebooting.");
    ESP.restart();
    delay(5000);
  }
  wifi_manager.autoConnect(my_hostname.c_str());
  
  Serial.println("");

  // Wait for connection
  while (WiFi.status() != WL_CONNECTED) {
    delay(500);
    Serial.print(".");
  }
  Serial.println("");
  Serial.println("Connected to wifi");
  Serial.println("Hostname: " + my_hostname);
  Serial.print("IP address: ");
  Serial.println(WiFi.localIP());

  if (mdns.begin(my_hostname.c_str())) {
    Serial.println("MDNS responder started");
  }

  server.on("/", [](){
    server.send(200, "text/plain", "root");
  });

  server.on("/mains", [](){
    server.send(200, "text/plain", String(pwm::get_mains_freq()));
  });

  server.on("/flash-size", [](){
    server.send(200, "text/plain", String(ESP.getFlashChipSizeByChipId()));
  });

  server.on("/set", [](){
    int val = server.arg("v").toInt();
    String client_name = server.arg("client");
    int slot_ms = server.arg("slot_ms").toInt();
    if (slot_ms == 0) {
        slot_ms = 1000 * 60;
    }
    slot_ms = std::min(slot_ms, 1000*60*5);
    slots::update(client_name, val, slot_ms);
    server.send(200, "text/plain", "PWM set to " + String(val));
  });

  server.on("/get", [](){
    server.send(200, "text/plain", slots::dump_state());
  });

#if 0
  server.on("/gpio", [](){
    int pin = server.arg("pin").toInt();
    int val = server.hasArg("val") ? server.arg("val").toInt() : -1;
    if (val == 0 || val == 1) {
      pinMode(pin, OUTPUT);
      digitalWrite(pin, val);
      server.send(200, "text/plain", "set");
    } else {
      pinMode(pin, INPUT);
      server.send(200, "text/plain", String(digitalRead(pin)));
    }
  });
#endif

  server.onNotFound([](){
    server.send(404, "text/plain", "Not found");
  });

  server.begin();
  mdns.addService("http", "tcp", 80);
  mdns.addService("budzik-v1", "tcp", 80);
  Serial.println("HTTP server started");
}

void loop(void){
  server.handleClient();
  pwm::set(slots::get_current_setting());
}
