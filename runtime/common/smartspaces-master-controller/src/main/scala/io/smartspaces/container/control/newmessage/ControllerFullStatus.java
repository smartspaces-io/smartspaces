/**
 * 
 */
package io.smartspaces.container.control.newmessage;

import java.util.List;

/**
 * @author keith
 *
 */
public class ControllerFullStatus {
	private String name;

	private String description;

	private String hostId;

	private List<LiveActivityRuntimeStatus> liveActivityStatuses;

	public String getName() {
		return name;
	}

	public void setName(String value) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getHostId() {
		return hostId;
	}

	public void setHostId(String hostId) {
		this.hostId = hostId;
	}

	public List<LiveActivityRuntimeStatus> getLiveActivityStatuses() {
		return liveActivityStatuses;
	}

	public void setLiveActivityStatuses(List<LiveActivityRuntimeStatus> liveActivityStatuses) {
		this.liveActivityStatuses = liveActivityStatuses;
	}

	@Override
	public String toString() {
		return "NewControllerFullStatus [name=" + name + ", description=" + description + ", hostId=" + hostId
				+ ", liveActivityStatuses=" + liveActivityStatuses + "]";
	}
}