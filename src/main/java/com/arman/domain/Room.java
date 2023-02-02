package com.arman.domain;

import java.io.Serializable;
import java.util.List;

public class Room implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private String address;
	
	private List<String> member;
	
	private String room_id;

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public List<String> getMember() {
		return member;
	}

	public void setMember(List<String> member) {
		this.member = member;
	}

	public String getRoom_id() {
		return room_id;
	}

	public void setRoom_id(String room_id) {
		this.room_id = room_id;
	}

	@Override
	public String toString() {
		return "Room [address=" + address + ", member=" + member + ", room_id=" + room_id + "]";
	}
	
}
