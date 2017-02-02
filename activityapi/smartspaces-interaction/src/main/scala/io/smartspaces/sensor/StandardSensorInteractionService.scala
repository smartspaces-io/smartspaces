package io.smartspaces.sensor

import io.smartspaces.service.BaseSupportedService
import io.smartspaces.sensor.integrator.SensorIntegrator

class StandardSensorInteractionService extends BaseSupportedService with SensorInteractionService {

  override def getName(): String = {
    SensorInteractionService.SERVICE_NAME
  }
  
  override def newSensorIntegrator(): SensorIntegrator = {
    null
  }
}