package com.haizhi.iap.follow.model.notification;

public class BasicNotification  extends Notification {

	public String cnname;
	public String enname;
	public int type;
	
	public BasicNotification() {}
	
	public BasicNotification(String cnname,String enname,int type) {
		this.cnname = cnname;
		this.enname = enname;
		this.type = type;
	}
	
	@Override
	public int getType() {
		return type;
	}
	
}
