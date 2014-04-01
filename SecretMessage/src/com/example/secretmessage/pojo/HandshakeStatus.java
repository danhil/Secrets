package com.example.secretmessage.pojo;

public enum HandshakeStatus {
	
	INIT((byte) 0xA),
	SENTPUBLIC((byte) 0xB),
	RECIEVEDPUBLIC((byte) 0xC),
	GENERATED((byte) 0XD),
	NOTYETENCRYPT((byte) 0x0);
	
	private byte status;
	private HandshakeStatus(byte status)
	{
		this.status = status;
	}
	
	public static HandshakeStatus getStatus(byte bs)
	{	
	
		for(HandshakeStatus hsStat : HandshakeStatus.values())
			if(hsStat.status == bs )
				return hsStat;
		return HandshakeStatus.NOTYETENCRYPT;
	}
	
	public byte getValue()
	{
		return this.status;
	}
}
