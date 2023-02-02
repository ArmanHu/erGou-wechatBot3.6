package com.arman.domain;

import java.io.Serializable;

public class Person implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private String wxid;
	
	private String nick;
	
	private String roomid;

	public String getWxid() {
		return wxid;
	}

	public void setWxid(String wxid) {
		this.wxid = wxid;
	}

	public String getNick() {
		return nick;
	}

	public void setNick(String nick) {
		this.nick = nick;
	}

	public String getRoomid() {
		return roomid;
	}

	public void setRoomid(String roomid) {
		this.roomid = roomid;
	}

	@Override
	public String toString() {
		return "Person [wxid=" + wxid + ", nick=" + nick + ", roomid=" + roomid + "]";
	}

	
}
