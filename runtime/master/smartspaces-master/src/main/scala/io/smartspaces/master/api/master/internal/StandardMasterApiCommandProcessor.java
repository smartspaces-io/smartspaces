/*
 * Copyright (C) 2016 Keith M. Hughes
 * Copyright (C) 2014 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package io.smartspaces.master.api.master.internal;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Lists;

import io.smartspaces.SimpleSmartSpacesException;
import io.smartspaces.SmartSpacesException;
import io.smartspaces.activity.ActivityState;
import io.smartspaces.domain.basic.LiveActivity;
import io.smartspaces.logging.ExtendedLog;
import io.smartspaces.master.api.master.MasterApiActivityManager;
import io.smartspaces.master.api.master.MasterApiAutomationManager;
import io.smartspaces.master.api.master.MasterApiMasterSupportManager;
import io.smartspaces.master.api.master.MasterApiSpaceControllerManager;
import io.smartspaces.master.api.messages.MasterApiMessages;
import io.smartspaces.master.server.services.ExtensionManager;
import io.smartspaces.master.server.services.model.ActiveLiveActivity;
import io.smartspaces.messaging.MessageSender;
import io.smartspaces.messaging.dynamic.SmartSpacesMessages;
import io.smartspaces.messaging.dynamic.SmartSpacesMessagesSupport;
import io.smartspaces.time.provider.TimeProvider;

/**
 * The Master API Command processor.
 * 
 * @author Keith M. Hughes
 */
public class StandardMasterApiCommandProcessor implements MasterApiCommandProcessor {

  /**
   * The Master API manager for activities.
   */
  private MasterApiActivityManager masterApiActivityManager;

  /**
   * The Master API manager for controllers.
   */
  private MasterApiSpaceControllerManager masterApiSpaceControllerManager;

  /**
   * The Master API manager for automation.
   */
  private MasterApiAutomationManager masterApiAutomationManager;

  /**
   * The Master API manager for master support.
   */
  private MasterApiMasterSupportManager masterApiMasterSupportManager;

  /**
   * The manager for extensions.
   */
  private ExtensionManager extensionManager;

  /**
   * The message sender for all clients.
   */
  private MessageSender<Map<String, Object>> allClientsMessageSender;

  /**
   * The time provider to use.
   */
  private TimeProvider timeProvider;

  /**
   * The logger to use.
   */
  private ExtendedLog log;

  /**
   * A mapping of command name to the handler for that command.
   */
  private final Map<String, MasterApiCommandHandler> commandHandlers = new HashMap<>();

  /**
   * Construct a new command processor.
   * 
   * @param masterApiActivityManager
   *          the Master API activity manager
   * @param masterApiSpaceControllerManager
   *          the Master API space controller manager
   * @param masterApiAutomationManager
   *          the Master API automation manager
   * @param masterApiMasterSupportManager
   *          the Master API master support manager
   * @param extensionManager
   *          the Master extension manager
   * @param timeProvider
   *          the time provider to use
   * @param allClientsMessageSender
   *          a sender for when all clients must be notified
   * @param log
   *          the logger
   */
  public StandardMasterApiCommandProcessor(MasterApiActivityManager masterApiActivityManager,
      MasterApiSpaceControllerManager masterApiSpaceControllerManager,
      MasterApiAutomationManager masterApiAutomationManager,
      MasterApiMasterSupportManager masterApiMasterSupportManager,
      ExtensionManager extensionManager, TimeProvider timeProvider,
      MessageSender<Map<String, Object>> allClientsMessageSender, ExtendedLog log) {
    this.masterApiActivityManager = masterApiActivityManager;
    this.masterApiSpaceControllerManager = masterApiSpaceControllerManager;
    this.masterApiAutomationManager = masterApiAutomationManager;
    this.masterApiMasterSupportManager = masterApiMasterSupportManager;
    this.extensionManager = extensionManager;
    this.timeProvider = timeProvider;
    this.allClientsMessageSender = allClientsMessageSender;
    this.log = log;

    registerAllCommandHandlers();
  }

  @Override
  @SuppressWarnings("unchecked")
  public void handleApiCall(Map<String, Object> message,
      MessageSender<Map<String, Object>> responseMessageSender) {
    if (log.isDebugEnabled()) {
      log.formatDebug("Master API websocket received request %s", message);
    }

    String command = (String) message.get(SmartSpacesMessages.MESSAGE_ENVELOPE_TYPE);
    Map<String, Object> commandArgs =
        (Map<String, Object>) message.get(SmartSpacesMessages.MESSAGE_ENVELOPE_DATA);

    String requestId = (String) message.get(SmartSpacesMessages.MESSAGE_ENVELOPE_REQUEST_ID);

    try {
      if (command.startsWith(MasterApiMessages.MASTER_API_COMMAND_EXTENSION_PREFIX)) {
        String extensionName =
            command.substring(MasterApiMessages.MASTER_API_COMMAND_EXTENSION_PREFIX.length());
        Map<String, Object> responseMessage =
            extensionManager.evaluateApiExtension(extensionName, commandArgs);
        responseMessage.put("command", command);
        responseMessage.put(SmartSpacesMessages.MESSAGE_ENVELOPE_TYPE,
            MasterApiMessages.MASTER_API_MESSAGE_TYPE_COMMAND_RESPONSE);
        potentiallyAddRequestId(responseMessage, requestId);

        sendResponseMessage(responseMessageSender, responseMessage);
      } else {
        executeWithCommandHandler(responseMessageSender, command, commandArgs, requestId);
      }
    } catch (Throwable e) {
      log.formatError(e, "Error while performing Master API command %s", command);

      Map<String, Object> responseMessage = SmartSpacesMessagesSupport
          .getFailureResponse(MasterApiMessages.MESSAGE_SPACE_CALL_FAILURE, e);
      potentiallyAddRequestId(responseMessage, requestId);

      try {
        sendResponseMessage(responseMessageSender, responseMessage);
      } catch (Throwable e1) {
        log.formatError(e1, "Error while responding to failure of Master API websocket command %s",
            command);
      }
    }
  }

  @Override
  public void sendLiveActivityStateChangeMessage(ActiveLiveActivity activeLiveActivity,
      ActivityState oldState, ActivityState newState) {
    LiveActivity liveActivity = activeLiveActivity.getLiveActivity();
    Map<String, Object> data = new HashMap<>();

    data.put(MasterApiMessages.MASTER_API_PARAMETER_NAME_STATUS_TYPE,
        MasterApiMessages.MASTER_API_PARAMETER_VALUE_TYPE_STATUS_LIVE_ACTIVITY);
    data.put(MasterApiMessages.MASTER_API_PARAMETER_NAME_ENTITY_UUID, liveActivity.getUuid());
    data.put(MasterApiMessages.MASTER_API_PARAMETER_NAME_ENTITY_ID, liveActivity.getId());
    data.put(MasterApiMessages.MASTER_API_PARAMETER_NAME_STATUS_RUNTIME_STATE, newState.name());
    data.put(MasterApiMessages.MASTER_API_PARAMETER_NAME_STATUS_RUNTIME_STATE_DESCRIPTION,
        newState.getDescription());
    data.put(MasterApiMessages.MASTER_API_PARAMETER_NAME_STATUS_DETAIL,
        activeLiveActivity.getRuntimeStateDetail());

    data.put(MasterApiMessages.MASTER_API_PARAMETER_NAME_STATUS_TIME,
        new Date(timeProvider.getCurrentTime()));

    Map<String, Object> message = new HashMap<>();
    message.put(SmartSpacesMessages.MESSAGE_ENVELOPE_TYPE,
        MasterApiMessages.MASTER_API_MESSAGE_TYPE_STATUS_UPDATE);
    message.put(SmartSpacesMessages.MESSAGE_ENVELOPE_DATA, data);

    allClientsMessageSender.sendMessage(message);
  }

  /**
   * Send the response message back to the client.
   *
   * @param messageSender
   *          the message sender that directs back to the client
   * @param responseMessage
   *          the response message
   */
  private void sendResponseMessage(MessageSender<Map<String, Object>> messageSender,
      Map<String, Object> responseMessage) {
    if (log.isDebugEnabled()) {
      log.formatDebug("Master API websocket responding with %s", responseMessage);
    }

    messageSender.sendMessage(responseMessage);
  }

  /**
   * Execute the command with a command handler.
   *
   * @param channelId
   *          the connection IS to the remote web socket client
   * @param command
   *          the command to be executed
   * @param commandArgs
   *          the arguments for the command, can be {@code null}
   * @param requestId
   *          the request ID for the command, can be {@code null}
   */
  @VisibleForTesting
  void executeWithCommandHandler(MessageSender<Map<String, Object>> messageSender, String command,
      Map<String, Object> commandArgs, String requestId) {
    MasterApiCommandHandler handler = commandHandlers.get(command);
    if (handler != null) {
      Map<String, Object> responseMessage = handler.execute(commandArgs);
      responseMessage.put(SmartSpacesMessages.MESSAGE_ENVELOPE_TYPE,
          MasterApiMessages.MASTER_API_MESSAGE_TYPE_COMMAND_RESPONSE);
      potentiallyAddRequestId(responseMessage, requestId);

      sendResponseMessage(messageSender, responseMessage);
    } else {
      log.formatError("Master API websocket connection got unknown command %s", command);
    }
  }

  /**
   * Add the request ID into the message if there is one.
   *
   * @param message
   *          the message
   * @param requestId
   *          the request ID
   */
  private void potentiallyAddRequestId(Map<String, Object> message, String requestId) {
    if (requestId != null) {
      message.put(SmartSpacesMessages.MESSAGE_ENVELOPE_REQUEST_ID, requestId);
    }
  }

  /**
   * Register all command handlers.
   */
  private void registerAllCommandHandlers() {
    registerActivityHandlers();
    registerLiveActivityHandlers();
    registerLiveActivityGroupHandlers();
    registerSpaceHandlers();
    registerSpaceControllerHandlers();
    registerMiscHandlers();
  }

  /**
   * Register all handlers for Live Activity commands.
   */
  private void registerActivityHandlers() {
    registerMasterApiCommandHandler(
        new MasterApiCommandHandler(MasterApiMessages.MASTER_API_COMMAND_ACTIVITY_ALL) {
          @Override
          public Map<String, Object> execute(Map<String, Object> commandArgs) {
            return masterApiActivityManager.getActivitiesByFilter(getFilter(commandArgs));
          }
        });
    registerMasterApiCommandHandler(
        new MasterApiCommandHandler(MasterApiMessages.MASTER_API_COMMAND_ACTIVITY_VIEW) {
          @Override
          public Map<String, Object> execute(Map<String, Object> commandArgs) {
            return masterApiActivityManager.getActivityView(getEntityId(commandArgs));
          }
        });
    registerMasterApiCommandHandler(
        new MasterApiCommandHandler(MasterApiMessages.MASTER_API_COMMAND_ACTIVITY_VIEW_FULL) {
          @Override
          public Map<String, Object> execute(Map<String, Object> commandArgs) {
            return masterApiActivityManager.getActivityFullView(getEntityId(commandArgs));
          }
        });
    registerMasterApiCommandHandler(
        new MasterApiCommandHandler(MasterApiMessages.MASTER_API_COMMAND_ACTIVITY_DEPLOY) {
          @Override
          public Map<String, Object> execute(Map<String, Object> commandArgs) {
            return masterApiSpaceControllerManager
                .deployAllActivityLiveActivities(getEntityId(commandArgs));
          }
        });
    registerMasterApiCommandHandler(
        new MasterApiCommandHandler(MasterApiMessages.MASTER_API_COMMAND_ACTIVITY_METADATA_SET) {
          @Override
          public Map<String, Object> execute(Map<String, Object> commandArgs) {
            String id = getEntityId(commandArgs);
            Map<String, Object> metadata = getRequiredMapArg(commandArgs,
                MasterApiMessages.MASTER_API_PARAMETER_NAME_ENTITY_METADATA);

            return masterApiActivityManager.updateActivityMetadata(id, metadata);
          }
        });
    registerMasterApiCommandHandler(
        new MasterApiCommandHandler(MasterApiMessages.MASTER_API_COMMAND_ACTIVITY_DELETE_LOCAL) {
          @Override
          public Map<String, Object> execute(Map<String, Object> commandArgs) {
            return masterApiActivityManager.deleteActivity(getEntityId(commandArgs));
          }
        });
  }

  /**
   * Register all handlers for Live Activity commands.
   */
  private void registerLiveActivityHandlers() {
    registerMasterApiCommandHandler(
        new MasterApiCommandHandler(MasterApiMessages.MASTER_API_COMMAND_LIVE_ACTIVITY_ALL) {
          @Override
          public Map<String, Object> execute(Map<String, Object> commandArgs) {
            return masterApiActivityManager.getLiveActivitiesByFilter(getFilter(commandArgs));
          }
        });
    registerMasterApiCommandHandler(
        new MasterApiCommandHandler(MasterApiMessages.MASTER_API_COMMAND_LIVE_ACTIVITY_VIEW) {
          @Override
          public Map<String, Object> execute(Map<String, Object> commandArgs) {
            return masterApiActivityManager.getLiveActivityView(getEntityId(commandArgs));
          }
        });
    registerMasterApiCommandHandler(
        new MasterApiCommandHandler(MasterApiMessages.MASTER_API_COMMAND_LIVE_ACTIVITY_VIEW_FULL) {
          @Override
          public Map<String, Object> execute(Map<String, Object> commandArgs) {
            return masterApiActivityManager.getLiveActivityFullView(getEntityId(commandArgs));
          }
        });
    registerMasterApiCommandHandler(
        new MasterApiCommandHandler(MasterApiMessages.MASTER_API_COMMAND_LIVE_ACTIVITY_CREATE) {
          @Override
          public Map<String, Object> execute(Map<String, Object> commandArgs) {
            return masterApiActivityManager.createLiveActivity(commandArgs);
          }
        });
    registerMasterApiCommandHandler(
        new MasterApiCommandHandler(MasterApiMessages.MASTER_API_COMMAND_LIVE_ACTIVITY_EDIT) {
          @Override
          public Map<String, Object> execute(Map<String, Object> commandArgs) {
            return masterApiActivityManager.editLiveActivity(commandArgs);
          }
        });
    registerMasterApiCommandHandler(
        new MasterApiCommandHandler(MasterApiMessages.MASTER_API_COMMAND_LIVE_ACTIVITY_DEPLOY) {
          @Override
          public Map<String, Object> execute(Map<String, Object> commandArgs) {
            return masterApiSpaceControllerManager.deployLiveActivity(getEntityId(commandArgs));
          }
        });
    registerMasterApiCommandHandler(
        new MasterApiCommandHandler(MasterApiMessages.MASTER_API_COMMAND_LIVE_ACTIVITY_CONFIGURE) {
          @Override
          public Map<String, Object> execute(Map<String, Object> commandArgs) {
            return masterApiSpaceControllerManager.configureLiveActivity(getEntityId(commandArgs));
          }
        });
    registerMasterApiCommandHandler(new MasterApiCommandHandler(
        MasterApiMessages.MASTER_API_COMMAND_LIVE_ACTIVITY_CONFIGURATION_GET) {
      @Override
      public Map<String, Object> execute(Map<String, Object> commandArgs) {
        return masterApiActivityManager.getLiveActivityConfiguration(getEntityId(commandArgs));
      }
    });
    registerMasterApiCommandHandler(new MasterApiCommandHandler(
        MasterApiMessages.MASTER_API_COMMAND_LIVE_ACTIVITY_CONFIGURATION_SET) {
      @Override
      public Map<String, Object> execute(Map<String, Object> commandArgs) {
        String id = getEntityId(commandArgs);
        Map<String, String> config = getConfiguration(commandArgs);
        return masterApiActivityManager.configureLiveActivity(id, config);
      }
    });
    registerMasterApiCommandHandler(new MasterApiCommandHandler(
        MasterApiMessages.MASTER_API_COMMAND_LIVE_ACTIVITY_METADATA_SET) {
      @Override
      public Map<String, Object> execute(Map<String, Object> commandArgs) {
        String id = getEntityId(commandArgs);
        Map<String, Object> metadata = getRequiredMapArg(commandArgs,
            MasterApiMessages.MASTER_API_PARAMETER_NAME_ENTITY_METADATA);

        return masterApiActivityManager.updateLiveActivityMetadata(id, metadata);
      }
    });
    registerMasterApiCommandHandler(
        new MasterApiCommandHandler(MasterApiMessages.MASTER_API_COMMAND_LIVE_ACTIVITY_STARTUP) {
          @Override
          public Map<String, Object> execute(Map<String, Object> commandArgs) {
            return masterApiSpaceControllerManager.startupLiveActivity(getEntityId(commandArgs));
          }
        });
    registerMasterApiCommandHandler(
        new MasterApiCommandHandler(MasterApiMessages.MASTER_API_COMMAND_LIVE_ACTIVITY_ACTIVATE) {
          @Override
          public Map<String, Object> execute(Map<String, Object> commandArgs) {
            return masterApiSpaceControllerManager.activateLiveActivity(getEntityId(commandArgs));
          }
        });
    registerMasterApiCommandHandler(
        new MasterApiCommandHandler(MasterApiMessages.MASTER_API_COMMAND_LIVE_ACTIVITY_DEACTIVATE) {
          @Override
          public Map<String, Object> execute(Map<String, Object> commandArgs) {
            return masterApiSpaceControllerManager.deactivateLiveActivity(getEntityId(commandArgs));
          }
        });
    registerMasterApiCommandHandler(
        new MasterApiCommandHandler(MasterApiMessages.MASTER_API_COMMAND_LIVE_ACTIVITY_SHUTDOWN) {
          @Override
          public Map<String, Object> execute(Map<String, Object> commandArgs) {
            return masterApiSpaceControllerManager.shutdownLiveActivity(getEntityId(commandArgs));
          }
        });
    registerMasterApiCommandHandler(
        new MasterApiCommandHandler(MasterApiMessages.MASTER_API_COMMAND_LIVE_ACTIVITY_STATUS) {
          @Override
          public Map<String, Object> execute(Map<String, Object> commandArgs) {
            return masterApiSpaceControllerManager.statusLiveActivity(getEntityId(commandArgs));
          }
        });
    registerMasterApiCommandHandler(new MasterApiCommandHandler(
        MasterApiMessages.MASTER_API_COMMAND_LIVE_ACTIVITY_DELETE_LOCAL) {
      @Override
      public Map<String, Object> execute(Map<String, Object> commandArgs) {
        return masterApiActivityManager.deleteLiveActivity(getEntityId(commandArgs));
      }
    });
    registerMasterApiCommandHandler(new MasterApiCommandHandler(
        MasterApiMessages.MASTER_API_COMMAND_LIVE_ACTIVITY_DELETE_REMOTE) {
      @Override
      public Map<String, Object> execute(Map<String, Object> commandArgs) {
        return masterApiSpaceControllerManager.deleteLiveActivity(getEntityId(commandArgs));
      }
    });
    registerMasterApiCommandHandler(new MasterApiCommandHandler(
        MasterApiMessages.MASTER_API_COMMAND_LIVE_ACTIVITY_DATA_PERMANENT_CLEAN) {
      @Override
      public Map<String, Object> execute(Map<String, Object> commandArgs) {
        return masterApiSpaceControllerManager
            .cleanLiveActivityPermanentData(getEntityId(commandArgs));
      }
    });
    registerMasterApiCommandHandler(new MasterApiCommandHandler(
        MasterApiMessages.MASTER_API_COMMAND_LIVE_ACTIVITY_DATA_TEMPORARY_CLEAN) {
      @Override
      public Map<String, Object> execute(Map<String, Object> commandArgs) {
        return masterApiSpaceControllerManager.cleanLiveActivityTempData(getEntityId(commandArgs));
      }
    });
  }

  /**
   * Register all handlers for Live Activity Group commands.
   */
  private void registerLiveActivityGroupHandlers() {
    registerMasterApiCommandHandler(
        new MasterApiCommandHandler(MasterApiMessages.MASTER_API_COMMAND_LIVE_ACTIVITY_GROUP_CREATE) {
          @Override
          public Map<String, Object> execute(Map<String, Object> commandArgs) {
            return masterApiActivityManager.createLiveActivityGroup(commandArgs);
          }
        });
    registerMasterApiCommandHandler(
        new MasterApiCommandHandler(MasterApiMessages.MASTER_API_COMMAND_LIVE_ACTIVITY_GROUP_ALL) {
          @Override
          public Map<String, Object> execute(Map<String, Object> commandArgs) {
            return masterApiActivityManager.getLiveActivityGroupsByFilter(getFilter(commandArgs));
          }
        });
    registerMasterApiCommandHandler(
        new MasterApiCommandHandler(MasterApiMessages.MASTER_API_COMMAND_LIVE_ACTIVITY_GROUP_VIEW) {
          @Override
          public Map<String, Object> execute(Map<String, Object> commandArgs) {
            return masterApiActivityManager.getLiveActivityGroupView(getEntityId(commandArgs));
          }
        });
    registerMasterApiCommandHandler(new MasterApiCommandHandler(
        MasterApiMessages.MASTER_API_COMMAND_LIVE_ACTIVITY_GROUP_VIEW_FULL) {
      @Override
      public Map<String, Object> execute(Map<String, Object> commandArgs) {
        return masterApiActivityManager.getLiveActivityGroupFullView(getEntityId(commandArgs));
      }
    });
    registerMasterApiCommandHandler(new MasterApiCommandHandler(
        MasterApiMessages.MASTER_API_COMMAND_LIVE_ACTIVITY_GROUP_DEPLOY) {
      @Override
      public Map<String, Object> execute(Map<String, Object> commandArgs) {
        return masterApiSpaceControllerManager.deployLiveActivityGroup(getEntityId(commandArgs));
      }
    });
    registerMasterApiCommandHandler(new MasterApiCommandHandler(
        MasterApiMessages.MASTER_API_COMMAND_LIVE_ACTIVITY_GROUP_CONFIGURE) {
      @Override
      public Map<String, Object> execute(Map<String, Object> commandArgs) {
        return masterApiSpaceControllerManager.configureLiveActivityGroup(getEntityId(commandArgs));
      }
    });
    registerMasterApiCommandHandler(new MasterApiCommandHandler(
        MasterApiMessages.MASTER_API_COMMAND_LIVE_ACTIVITY_GROUP_METADATA_SET) {
      @Override
      public Map<String, Object> execute(Map<String, Object> commandArgs) {
        String id = getEntityId(commandArgs);
        Map<String, Object> metadata = getRequiredMapArg(commandArgs,
            MasterApiMessages.MASTER_API_PARAMETER_NAME_ENTITY_METADATA);

        return masterApiActivityManager.updateLiveActivityGroupMetadata(id, metadata);
      }
    });
    registerMasterApiCommandHandler(new MasterApiCommandHandler(
        MasterApiMessages.MASTER_API_COMMAND_LIVE_ACTIVITY_GROUP_STARTUP) {
      @Override
      public Map<String, Object> execute(Map<String, Object> commandArgs) {
        return masterApiSpaceControllerManager.startupLiveActivityGroup(getEntityId(commandArgs));
      }
    });
    registerMasterApiCommandHandler(new MasterApiCommandHandler(
        MasterApiMessages.MASTER_API_COMMAND_LIVE_ACTIVITY_GROUP_ACTIVATE) {
      @Override
      public Map<String, Object> execute(Map<String, Object> commandArgs) {
        return masterApiSpaceControllerManager.activateLiveActivityGroup(getEntityId(commandArgs));
      }
    });
    registerMasterApiCommandHandler(new MasterApiCommandHandler(
        MasterApiMessages.MASTER_API_COMMAND_LIVE_ACTIVITY_GROUP_DEACTIVATE) {
      @Override
      public Map<String, Object> execute(Map<String, Object> commandArgs) {
        return masterApiSpaceControllerManager
            .deactivateLiveActivityGroup(getEntityId(commandArgs));
      }
    });
    registerMasterApiCommandHandler(new MasterApiCommandHandler(
        MasterApiMessages.MASTER_API_COMMAND_LIVE_ACTIVITY_GROUP_SHUTDOWN) {
      @Override
      public Map<String, Object> execute(Map<String, Object> commandArgs) {
        return masterApiSpaceControllerManager.shutdownLiveActivityGroup(getEntityId(commandArgs));
      }
    });
    registerMasterApiCommandHandler(new MasterApiCommandHandler(
        MasterApiMessages.MASTER_API_COMMAND_LIVE_ACTIVITY_GROUP_STATUS) {
      @Override
      public Map<String, Object> execute(Map<String, Object> commandArgs) {
        return masterApiSpaceControllerManager.statusLiveActivityGroup(getEntityId(commandArgs));
      }
    });
    registerMasterApiCommandHandler(new MasterApiCommandHandler(
        MasterApiMessages.MASTER_API_COMMAND_LIVE_ACTIVITY_GROUP_SHUTDOWN_FORCE) {
      @Override
      public Map<String, Object> execute(Map<String, Object> commandArgs) {
        return masterApiSpaceControllerManager
            .forceShutdownLiveActivitiesLiveActivityGroup(getEntityId(commandArgs));
      }
    });
    registerMasterApiCommandHandler(new MasterApiCommandHandler(
        MasterApiMessages.MASTER_API_COMMAND_LIVE_ACTIVITY_GROUP_DELETE_LOCAL) {
      @Override
      public Map<String, Object> execute(Map<String, Object> commandArgs) {
        return masterApiActivityManager.deleteLiveActivityGroup(getEntityId(commandArgs));
      }
    });
  }

  /**
   * Register all handlers for Space commands.
   */
  private void registerSpaceHandlers() {
    registerMasterApiCommandHandler(
        new MasterApiCommandHandler(MasterApiMessages.MASTER_API_COMMAND_SPACE_ALL) {
          @Override
          public Map<String, Object> execute(Map<String, Object> commandArgs) {
            return masterApiActivityManager.getSpacesByFilter(getFilter(commandArgs));
          }
        });
    registerMasterApiCommandHandler(
        new MasterApiCommandHandler(MasterApiMessages.MASTER_API_COMMAND_SPACE_VIEW) {
          @Override
          public Map<String, Object> execute(Map<String, Object> commandArgs) {
            return masterApiActivityManager.getSpaceView(getEntityId(commandArgs));
          }
        });
    registerMasterApiCommandHandler(
        new MasterApiCommandHandler(MasterApiMessages.MASTER_API_COMMAND_SPACE_VIEW_FULL) {
          @Override
          public Map<String, Object> execute(Map<String, Object> commandArgs) {
            return masterApiActivityManager.getSpaceFullView(getEntityId(commandArgs));
          }
        });
    registerMasterApiCommandHandler(
        new MasterApiCommandHandler(MasterApiMessages.MASTER_API_COMMAND_SPACE_DEPLOY) {
          @Override
          public Map<String, Object> execute(Map<String, Object> commandArgs) {
            return masterApiSpaceControllerManager.deploySpace(getEntityId(commandArgs));
          }
        });
    registerMasterApiCommandHandler(
        new MasterApiCommandHandler(MasterApiMessages.MASTER_API_COMMAND_SPACE_CONFIGURE) {
          @Override
          public Map<String, Object> execute(Map<String, Object> commandArgs) {
            return masterApiSpaceControllerManager.configureSpace(getEntityId(commandArgs));
          }
        });
    registerMasterApiCommandHandler(
        new MasterApiCommandHandler(MasterApiMessages.MASTER_API_COMMAND_SPACE_METADATA_SET) {
          @Override
          public Map<String, Object> execute(Map<String, Object> commandArgs) {
            String id = getEntityId(commandArgs);
            Map<String, Object> metadata = getRequiredMapArg(commandArgs,
                MasterApiMessages.MASTER_API_PARAMETER_NAME_ENTITY_METADATA);

            return masterApiActivityManager.updateSpaceMetadata(id, metadata);
          }
        });
    registerMasterApiCommandHandler(
        new MasterApiCommandHandler(MasterApiMessages.MASTER_API_COMMAND_SPACE_STARTUP) {
          @Override
          public Map<String, Object> execute(Map<String, Object> commandArgs) {
            return masterApiSpaceControllerManager.startupSpace(getEntityId(commandArgs));
          }
        });
    registerMasterApiCommandHandler(
        new MasterApiCommandHandler(MasterApiMessages.MASTER_API_COMMAND_SPACE_ACTIVATE) {
          @Override
          public Map<String, Object> execute(Map<String, Object> commandArgs) {
            return masterApiSpaceControllerManager.activateSpace(getEntityId(commandArgs));
          }
        });
    registerMasterApiCommandHandler(
        new MasterApiCommandHandler(MasterApiMessages.MASTER_API_COMMAND_SPACE_DEACTIVATE) {
          @Override
          public Map<String, Object> execute(Map<String, Object> commandArgs) {
            return masterApiSpaceControllerManager.deactivateSpace(getEntityId(commandArgs));
          }
        });
    registerMasterApiCommandHandler(
        new MasterApiCommandHandler(MasterApiMessages.MASTER_API_COMMAND_SPACE_SHUTDOWN) {
          @Override
          public Map<String, Object> execute(Map<String, Object> commandArgs) {
            return masterApiSpaceControllerManager.shutdownSpace(getEntityId(commandArgs));
          }
        });
    registerMasterApiCommandHandler(
        new MasterApiCommandHandler(MasterApiMessages.MASTER_API_COMMAND_SPACE_STATUS) {
          @Override
          public Map<String, Object> execute(Map<String, Object> commandArgs) {
            return masterApiSpaceControllerManager.statusSpace(getEntityId(commandArgs));
          }
        });
    registerMasterApiCommandHandler(
        new MasterApiCommandHandler(MasterApiMessages.MASTER_API_COMMAND_SPACE_DELETE_LOCAL) {
          @Override
          public Map<String, Object> execute(Map<String, Object> commandArgs) {
            return masterApiActivityManager.deleteSpace(getEntityId(commandArgs));
          }
        });
  }

  /**
   * Register all handlers for Space Controller commands.
   */
  private void registerSpaceControllerHandlers() {
    registerMasterApiCommandHandler(
        new MasterApiCommandHandler(MasterApiMessages.MASTER_API_COMMAND_SPACE_CONTROLLER_ALL) {
          @Override
          public Map<String, Object> execute(Map<String, Object> commandArgs) {
            return masterApiSpaceControllerManager
                .getSpaceControllersByFilter(getFilter(commandArgs));
          }
        });
    registerMasterApiCommandHandler(
        new MasterApiCommandHandler(MasterApiMessages.MASTER_API_COMMAND_SPACE_CONTROLLER_VIEW) {
          @Override
          public Map<String, Object> execute(Map<String, Object> commandArgs) {
            return masterApiSpaceControllerManager.getSpaceControllerView(getEntityId(commandArgs));
          }
        });
    registerMasterApiCommandHandler(new MasterApiCommandHandler(
        MasterApiMessages.MASTER_API_COMMAND_SPACE_CONTROLLER_VIEW_FULL) {
      @Override
      public Map<String, Object> execute(Map<String, Object> commandArgs) {
        return masterApiSpaceControllerManager.getSpaceControllerFullView(getEntityId(commandArgs));
      }
    });
    registerMasterApiCommandHandler(new MasterApiCommandHandler(
        MasterApiMessages.MASTER_API_COMMAND_SPACE_CONTROLLER_CONFIGURATION_GET) {
      @Override
      public Map<String, Object> execute(Map<String, Object> commandArgs) {
        return masterApiSpaceControllerManager
            .getSpaceControllerConfiguration(getEntityId(commandArgs));
      }
    });
    registerMasterApiCommandHandler(new MasterApiCommandHandler(
        MasterApiMessages.MASTER_API_COMMAND_SPACE_CONTROLLER_CONFIGURATION_SET) {
      @Override
      public Map<String, Object> execute(Map<String, Object> commandArgs) {
        String id = getEntityId(commandArgs);
        Map<String, String> config = getConfiguration(commandArgs);
        return masterApiSpaceControllerManager.setSpaceControllerConfiguration(id, config);
      }
    });
    registerMasterApiCommandHandler(new MasterApiCommandHandler(
        MasterApiMessages.MASTER_API_COMMAND_SPACE_CONTROLLER_CONFIGURE) {
      @Override
      public Map<String, Object> execute(Map<String, Object> commandArgs) {
        return masterApiSpaceControllerManager.configureSpaceController(getEntityId(commandArgs));
      }
    });
    registerMasterApiCommandHandler(new MasterApiCommandHandler(
        MasterApiMessages.MASTER_API_COMMAND_SPACE_CONTROLLER_METADATA_SET) {
      @Override
      public Map<String, Object> execute(Map<String, Object> commandArgs) {
        String id = getEntityId(commandArgs);
        Map<String, Object> metadata = getRequiredMapArg(commandArgs,
            MasterApiMessages.MASTER_API_PARAMETER_NAME_ENTITY_METADATA);

        return masterApiSpaceControllerManager.updateSpaceControllerMetadata(id, metadata);
      }
    });
    registerMasterApiCommandHandler(
        new MasterApiCommandHandler(MasterApiMessages.MASTER_API_COMMAND_SPACE_CONTROLLER_CONNECT) {
          @Override
          public Map<String, Object> execute(Map<String, Object> commandArgs) {
            return masterApiSpaceControllerManager
                .connectToSpaceControllers(Lists.newArrayList(getEntityId(commandArgs)));
          }
        });
    registerMasterApiCommandHandler(new MasterApiCommandHandler(
        MasterApiMessages.MASTER_API_COMMAND_SPACE_CONTROLLER_DISCONNECT) {
      @Override
      public Map<String, Object> execute(Map<String, Object> commandArgs) {
        return masterApiSpaceControllerManager
            .disconnectFromSpaceControllers(Lists.newArrayList(getEntityId(commandArgs)));
      }
    });
    registerMasterApiCommandHandler(new MasterApiCommandHandler(
        MasterApiMessages.MASTER_API_COMMAND_SPACE_CONTROLLER_CONNECT_ALL) {
      @Override
      public Map<String, Object> execute(Map<String, Object> commandArgs) {
        return masterApiSpaceControllerManager.connectToAllSpaceControllers();
      }
    });
    registerMasterApiCommandHandler(new MasterApiCommandHandler(
        MasterApiMessages.MASTER_API_COMMAND_SPACE_CONTROLLER_DISCONNECT_ALL) {
      @Override
      public Map<String, Object> execute(Map<String, Object> commandArgs) {
        return masterApiSpaceControllerManager.disconnectFromAllSpaceControllers();
      }
    });
    registerMasterApiCommandHandler(
        new MasterApiCommandHandler(MasterApiMessages.MASTER_API_COMMAND_SPACE_CONTROLLER_STATUS) {
          @Override
          public Map<String, Object> execute(Map<String, Object> commandArgs) {
            return masterApiSpaceControllerManager
                .statusSpaceControllers(Lists.newArrayList(getEntityId(commandArgs)));
          }
        });
    registerMasterApiCommandHandler(new MasterApiCommandHandler(
        MasterApiMessages.MASTER_API_COMMAND_SPACE_CONTROLLER_STATUS_ALL) {
      @Override
      public Map<String, Object> execute(Map<String, Object> commandArgs) {
        return masterApiSpaceControllerManager.statusFromAllSpaceControllers();
      }
    });
    registerMasterApiCommandHandler(
        new MasterApiCommandHandler(MasterApiMessages.MASTER_API_COMMAND_SPACE_CONTROLLER_DEPLOY) {
          @Override
          public Map<String, Object> execute(Map<String, Object> commandArgs) {
            return masterApiSpaceControllerManager
                .deployAllLiveActivitiesSpaceController(getEntityId(commandArgs));
          }
        });
    registerMasterApiCommandHandler(new MasterApiCommandHandler(
        MasterApiMessages.MASTER_API_COMMAND_SPACE_CONTROLLER_DEPLOY_ALL) {
      @Override
      public Map<String, Object> execute(Map<String, Object> commandArgs) {
        return masterApiSpaceControllerManager.deployAllLiveActivitiesAllSpaceControllers();
      }
    });
    registerMasterApiCommandHandler(new MasterApiCommandHandler(
        MasterApiMessages.MASTER_API_COMMAND_SPACE_CONTROLLER_DATA_PERMANENT_CLEAN) {
      @Override
      public Map<String, Object> execute(Map<String, Object> commandArgs) {
        return masterApiSpaceControllerManager
            .cleanSpaceControllerPermanentData(getEntityId(commandArgs));
      }
    });
    registerMasterApiCommandHandler(new MasterApiCommandHandler(
        MasterApiMessages.MASTER_API_COMMAND_SPACE_CONTROLLER_DATA_TEMPORARY_CLEAN) {
      @Override
      public Map<String, Object> execute(Map<String, Object> commandArgs) {
        return masterApiSpaceControllerManager
            .cleanSpaceControllerTempData(getEntityId(commandArgs));
      }
    });
    registerMasterApiCommandHandler(new MasterApiCommandHandler(
        MasterApiMessages.MASTER_API_COMMAND_SPACE_CONTROLLER_DATA_PERMANENT_CLEAN_ALL) {
      @Override
      public Map<String, Object> execute(Map<String, Object> commandArgs) {
        return masterApiSpaceControllerManager
            .cleanSpaceControllerPermanentDataAllSpaceControllers();
      }
    });
    registerMasterApiCommandHandler(new MasterApiCommandHandler(
        MasterApiMessages.MASTER_API_COMMAND_SPACE_CONTROLLER_DATA_TEMPORARY_CLEAN_ALL) {
      @Override
      public Map<String, Object> execute(Map<String, Object> commandArgs) {
        return masterApiSpaceControllerManager.cleanSpaceControllerTempDataAllSpaceControllers();
      }
    });
    registerMasterApiCommandHandler(new MasterApiCommandHandler(
        MasterApiMessages.MASTER_API_COMMAND_SPACE_CONTROLLER_LIVE_ACTIVITY_DATA_PERMANENT_CLEAN) {
      @Override
      public Map<String, Object> execute(Map<String, Object> commandArgs) {
        return masterApiSpaceControllerManager
            .cleanSpaceControllerActivitiesPermanentData(getEntityId(commandArgs));
      }
    });
    registerMasterApiCommandHandler(new MasterApiCommandHandler(
        MasterApiMessages.MASTER_API_COMMAND_SPACE_CONTROLLER_LIVE_ACTIVITY_DATA_TEMPORARY_CLEAN) {
      @Override
      public Map<String, Object> execute(Map<String, Object> commandArgs) {
        return masterApiSpaceControllerManager
            .cleanSpaceControllerActivitiesTempData(getEntityId(commandArgs));
      }
    });
    registerMasterApiCommandHandler(new MasterApiCommandHandler(
        MasterApiMessages.MASTER_API_COMMAND_SPACE_CONTROLLER_ALL_LIVE_ACTIVITY_DATA_PERMANENT_CLEAN) {
      @Override
      public Map<String, Object> execute(Map<String, Object> commandArgs) {
        return masterApiSpaceControllerManager
            .cleanSpaceControllerActivitiesPermanentDataAllSpaceControllers();
      }
    });
    registerMasterApiCommandHandler(new MasterApiCommandHandler(
        MasterApiMessages.MASTER_API_COMMAND_SPACE_CONTROLLER_ALL_LIVE_ACTIVITY_DATA_TEMPORARY_CLEAN) {
      @Override
      public Map<String, Object> execute(Map<String, Object> commandArgs) {
        return masterApiSpaceControllerManager
            .cleanSpaceControllerActivitiesTempDataAllSpaceControllers();
      }
    });
    registerMasterApiCommandHandler(new MasterApiCommandHandler(
        MasterApiMessages.MASTER_API_COMMAND_SPACE_CONTROLLER_SHUTDOWN) {
      @Override
      public Map<String, Object> execute(Map<String, Object> commandArgs) {
        return masterApiSpaceControllerManager
            .shutdownSpaceControllers(Lists.newArrayList(getEntityId(commandArgs)));
      }
    });
    registerMasterApiCommandHandler(new MasterApiCommandHandler(
        MasterApiMessages.MASTER_API_COMMAND_SPACE_CONTROLLER_RESTART_HARD) {
      @Override
      public Map<String, Object> execute(Map<String, Object> commandArgs) {
        return masterApiSpaceControllerManager
            .hardRestartSpaceControllers(Lists.newArrayList(getEntityId(commandArgs)));
      }
    });
    registerMasterApiCommandHandler(new MasterApiCommandHandler(
        MasterApiMessages.MASTER_API_COMMAND_SPACE_CONTROLLER_RESTART_SOFT) {
      @Override
      public Map<String, Object> execute(Map<String, Object> commandArgs) {
        return masterApiSpaceControllerManager
            .softRestartSpaceControllers(Lists.newArrayList(getEntityId(commandArgs)));
      }
    });
    registerMasterApiCommandHandler(new MasterApiCommandHandler(
        MasterApiMessages.MASTER_API_COMMAND_SPACE_CONTROLLER_LIVE_ACTIVITY_SHUTDOWN_ALL) {
      @Override
      public Map<String, Object> execute(Map<String, Object> commandArgs) {
        return masterApiSpaceControllerManager.shutdownAllLiveActivities(getEntityId(commandArgs));
      }
    });
    registerMasterApiCommandHandler(new MasterApiCommandHandler(
        MasterApiMessages.MASTER_API_COMMAND_SPACE_CONTROLLER_ALL_SHUTDOWN) {
      @Override
      public Map<String, Object> execute(Map<String, Object> commandArgs) {
        return masterApiSpaceControllerManager.shutdownAllSpaceControllers();
      }
    });
    registerMasterApiCommandHandler(new MasterApiCommandHandler(
        MasterApiMessages.MASTER_API_COMMAND_SPACE_CONTROLLER_ALL_RESTART_HARD) {
      @Override
      public Map<String, Object> execute(Map<String, Object> commandArgs) {
        return masterApiSpaceControllerManager.hardRestartAllSpaceControllers();
      }
    });
    registerMasterApiCommandHandler(new MasterApiCommandHandler(
        MasterApiMessages.MASTER_API_COMMAND_SPACE_CONTROLLER_ALL_RESTART_SOFT) {
      @Override
      public Map<String, Object> execute(Map<String, Object> commandArgs) {
        return masterApiSpaceControllerManager.softRestartAllSpaceControllers();
      }
    });
    registerMasterApiCommandHandler(new MasterApiCommandHandler(
        MasterApiMessages.MASTER_API_COMMAND_SPACE_CONTROLLER_ALL_LIVE_ACTIVITY_SHUTDOWN_ALL) {
      @Override
      public Map<String, Object> execute(Map<String, Object> commandArgs) {
        return masterApiSpaceControllerManager.shutdownAllLiveActivitiesAllSpaceControllers();
      }
    });
    registerMasterApiCommandHandler(new MasterApiCommandHandler(
        MasterApiMessages.MASTER_API_COMMAND_SPACE_CONTROLLER_DATA_CAPTURE) {
      @Override
      public Map<String, Object> execute(Map<String, Object> commandArgs) {
        return masterApiSpaceControllerManager.captureDataSpaceController(getEntityId(commandArgs));
      }
    });
    registerMasterApiCommandHandler(new MasterApiCommandHandler(
        MasterApiMessages.MASTER_API_COMMAND_SPACE_CONTROLLER_DATA_RESTORE) {
      @Override
      public Map<String, Object> execute(Map<String, Object> commandArgs) {
        return masterApiSpaceControllerManager.restoreDataSpaceController(getEntityId(commandArgs));
      }
    });
    registerMasterApiCommandHandler(new MasterApiCommandHandler(
        MasterApiMessages.MASTER_API_COMMAND_SPACE_CONTROLLER_ALL_DATA_CAPTURE) {
      @Override
      public Map<String, Object> execute(Map<String, Object> commandArgs) {
        return masterApiSpaceControllerManager.captureDataAllSpaceControllers();
      }
    });
    registerMasterApiCommandHandler(new MasterApiCommandHandler(
        MasterApiMessages.MASTER_API_COMMAND_SPACE_CONTROLLER_ALL_DATA_RESTORE) {
      @Override
      public Map<String, Object> execute(Map<String, Object> commandArgs) {
        return masterApiSpaceControllerManager.restoreDataAllSpaceControllers();
      }
    });
    registerMasterApiCommandHandler(new MasterApiCommandHandler(
        MasterApiMessages.MASTER_API_COMMAND_SPACE_CONTROLLER_DELETE_LOCAL) {
      @Override
      public Map<String, Object> execute(Map<String, Object> commandArgs) {
        return masterApiSpaceControllerManager.deleteSpaceController(getEntityId(commandArgs));
      }
    });
  }

  /**
   * Register all handlers for misc commands.
   */
  private void registerMiscHandlers() {
    registerMasterApiCommandHandler(
        new MasterApiCommandHandler(MasterApiMessages.MASTER_API_COMMAND_NAMEDSCRIPT_ALL) {
          @Override
          public Map<String, Object> execute(Map<String, Object> commandArgs) {
            return masterApiAutomationManager.getNamedScriptsByFilter(getFilter(commandArgs));
          }
        });
    registerMasterApiCommandHandler(
        new MasterApiCommandHandler(MasterApiMessages.MASTER_API_COMMAND_NAMEDSCRIPT_VIEW) {
          @Override
          public Map<String, Object> execute(Map<String, Object> commandArgs) {
            return masterApiAutomationManager.getNamedScriptView(getEntityId(commandArgs));
          }
        });
    registerMasterApiCommandHandler(
        new MasterApiCommandHandler(MasterApiMessages.MASTER_API_COMMAND_NAMEDSCRIPT_RUN) {
          @Override
          public Map<String, Object> execute(Map<String, Object> commandArgs) {
            return masterApiAutomationManager.runNamedScript(getEntityId(commandArgs));
          }
        });
    registerMasterApiCommandHandler(
        new MasterApiCommandHandler(MasterApiMessages.MASTER_API_COMMAND_NAMEDSCRIPT_METADATA_SET) {
          @Override
          public Map<String, Object> execute(Map<String, Object> commandArgs) {
            String id = getEntityId(commandArgs);
            Map<String, Object> metadata = getRequiredMapArg(commandArgs,
                MasterApiMessages.MASTER_API_PARAMETER_NAME_ENTITY_METADATA);

            return masterApiAutomationManager.updateNamedScriptMetadata(id, metadata);
          }
        });
    registerMasterApiCommandHandler(
        new MasterApiCommandHandler(MasterApiMessages.MASTER_API_COMMAND_NAMEDSCRIPT_DELETE) {
          @Override
          public Map<String, Object> execute(Map<String, Object> commandArgs) {
            return masterApiAutomationManager.deleteNamedScript(getEntityId(commandArgs));
          }
        });

    registerMasterApiCommandHandler(new MasterApiCommandHandler(
        MasterApiMessages.MASTER_API_COMMAND_ADMIN_MASTER_DOMAIN_MODEL_EXPORT) {
      @Override
      public Map<String, Object> execute(Map<String, Object> commandArgs) {
        return masterApiMasterSupportManager.exportMasterDomainModel();
      }
    });
    registerMasterApiCommandHandler(new MasterApiCommandHandler(
        MasterApiMessages.MASTER_API_COMMAND_ADMIN_MASTER_DOMAIN_MODEL_IMPORT) {
      @Override
      public Map<String, Object> execute(Map<String, Object> commandArgs) {
        String model = (String) commandArgs.get(MasterApiMessages.MASTER_API_PARAMETER_NAME_MODEL);
        return masterApiMasterSupportManager.importMasterDomainModel(model);
      }
    });
    registerMasterApiCommandHandler(
        new MasterApiCommandHandler(MasterApiMessages.MASTER_API_COMMAND_SMART_SPACES_VERSION) {
          @Override
          public Map<String, Object> execute(Map<String, Object> commandArgs) {
            return masterApiMasterSupportManager.getSmartSpacesVersion();
          }
        });
  }

  /**
   * Register a command handler with the manager.
   *
   * @param handler
   *          the command handler
   */
  private void registerMasterApiCommandHandler(MasterApiCommandHandler handler) {
    commandHandlers.put(handler.getCommandName(), handler);
  }

  /**
   * Command handler for a Master API call.
   *
   * @author Keith M. Hughes
   */
  private abstract class MasterApiCommandHandler {

    /**
     * The name of the command.
     */
    private final String commandName;

    /**
     * Create a command handler.
     *
     * @param commandName
     *          the command name
     */
    public MasterApiCommandHandler(String commandName) {
      this.commandName = commandName;
    }

    /**
     * Get the name of the command.
     *
     * @return the name of the command
     */
    public String getCommandName() {
      return commandName;
    }

    /**
     * Execute the command.
     *
     * @param commandArgs
     *          the arguments for the command
     *
     * @return the result of the command
     */
    public abstract Map<String, Object> execute(Map<String, Object> commandArgs);

    /**
     * Get a required string argument from the args map.
     *
     * @param args
     *          the args map
     * @param argName
     *          the argument
     *
     * @return the value of the arg
     *
     * @throws SimpleSmartSpacesException
     *           if there is no value for the requested arg
     */
    protected String getRequiredStringArg(Map<String, Object> args, String argName)
        throws SimpleSmartSpacesException {
      String value = (String) args.get(argName);
      if (value != null) {
        return value;
      } else {
        throw new SimpleSmartSpacesException("Unknown argument " + argName);
      }
    }

    /**
     * Get a required string argument from the args map.
     *
     * @param args
     *          the args map
     * @param argName
     *          the argument
     * @param <K>
     *          type for keys in the map
     * @param <V>
     *          type for values in the map
     *
     * @return the value of the arg
     *
     * @throws SimpleSmartSpacesException
     *           if there is no value for the requested arg
     */
    protected <K, V> Map<K, V> getRequiredMapArg(Map<String, Object> args, String argName)
        throws SimpleSmartSpacesException {
      Object value = args.get(argName);
      if (value != null) {
        if (Map.class.isAssignableFrom(value.getClass())) {
          @SuppressWarnings("unchecked")
          Map<K, V> value2 = (Map<K, V>) value;
          return value2;
        } else {
          throw new SimpleSmartSpacesException("Argument not map " + argName);
        }
      } else {
        throw new SimpleSmartSpacesException("Unknown argument " + argName);
      }
    }

    /**
     * Get the filter parameter from the command arguments.
     *
     * @param commandArgs
     *          the command arguments
     *
     * @return the filter, or {code null} if none
     */
    protected String getFilter(Map<String, Object> commandArgs) {
      return (commandArgs != null)
          ? (String) commandArgs.get(MasterApiMessages.MASTER_API_PARAMETER_NAME_FILTER)
          : null;
    }

    /**
     * Get the entity ID from the command arguments.
     *
     * @param commandArgs
     *          the command arguments
     *
     * @return the entity ID
     *
     * @throws SmartSpacesException
     *           the entity ID was not in the command arguments
     */
    protected String getEntityId(Map<String, Object> commandArgs) throws SmartSpacesException {
      return getRequiredStringArg(commandArgs,
          MasterApiMessages.MASTER_API_PARAMETER_NAME_ENTITY_ID);
    }

    /**
     * Get the configuration map from the command arguments.
     *
     * @param commandArgs
     *          the command arguments
     *
     * @return the configuration map
     *
     * @throws SmartSpacesException
     *           the configuration map was not in the command arguments
     */
    protected Map<String, String> getConfiguration(Map<String, Object> commandArgs)
        throws SmartSpacesException {
      return getRequiredMapArg(commandArgs,
          MasterApiMessages.MASTER_API_PARAMETER_NAME_ENTITY_CONFIG);
    }
  }
}
