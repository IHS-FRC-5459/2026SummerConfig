package org.team5459.config;

import frc.robot.util.Elastic;
import frc.robot.util.Elastic.Notification;
import frc.robot.util.Elastic.NotificationLevel;

/** ElasticLib toasts for dashboard Create / Delete / Save actions. */
final class ConfigNotifications {
  private ConfigNotifications() {}

  static void success(String title, String description) {
    send(NotificationLevel.INFO, title, description);
  }

  static void failure(String title, String description) {
    send(NotificationLevel.WARNING, title, description);
  }

  static void error(String title, String description) {
    send(NotificationLevel.ERROR, title, description);
  }

  private static void send(NotificationLevel level, String title, String description) {
    Elastic.sendNotification(
        new Notification()
            .withLevel(level)
            .withTitle(title)
            .withDescription(description)
            .withDisplaySeconds(3.0));
  }
}
