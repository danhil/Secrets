package com.example.secretmessage.pojo;

public enum HandshakeStatus {
	
	INIT("init"),
	RECIEVEDPUBLIC("recpub"),
	SENTPUBLIC("sentpub"),
	GENERATED("generated"),
	NOTYETENCRYPT("notEncrypt");
	
	private String status;
	private HandshakeStatus(String status)
	{
		this.status = status;
	}
	
	public static HandshakeStatus getStatus(String status)
	{
		for(HandshakeStatus hsStat : HandshakeStatus.values())
			if(hsStat.status.contains(status))
				return hsStat;
		throw new UnsupportedOperationException("The handshake status is not recognized");
	}
	
	public String getValue()
	{
		return this.status;
	}
}
