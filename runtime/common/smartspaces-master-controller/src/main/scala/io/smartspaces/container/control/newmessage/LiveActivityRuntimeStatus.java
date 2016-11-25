package io.smartspaces.container.control.newmessage;

import io.smartspaces.activity.ActivityState;

public class LiveActivityRuntimeStatus {
	private String uuid;

	private ActivityState status;

	private String statusCode;

	private String statusDetail;

	public String getUuid() {
		return uuid;
	}

	public void setUuid(String uuid) {
		this.uuid = uuid;
	}

	public ActivityState getStatus() {
		return status;
	}

	public void setStatus(ActivityState status) {
		this.status = status;
	}

	public String getStatusCode() {
		return statusCode;
	}

	public void setStatusCode(String statusCode) {
		this.statusCode = statusCode;
	}

	public String getStatusDetail() {
		return statusDetail;
	}

	public void setStatusDetail(String statusDetail) {
		this.statusDetail = statusDetail;
	}

	@Override
	public String toString() {
		return "NewLiveActivityRuntimeStatus [uuid=" + uuid + ", status=" + status + ", statusCode=" + statusCode
				+ ", statusDetail=" + statusDetail + "]";
	}
}
