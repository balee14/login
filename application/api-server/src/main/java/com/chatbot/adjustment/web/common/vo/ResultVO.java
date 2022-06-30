package com.chatbot.adjustment.web.common.vo;

public class ResultVO {
	
	private Object items;
	private String resultCode = "";
	private String resultMessage = ""; 
	
	public ResultVO() {
	}
	
	public ResultVO( Object items, String resultCode, String resultMessage ) {
		this.items = items;
		this.resultCode = resultCode;
		this.resultMessage = resultMessage;
	}
	
	public Object getItems() {
		return items;
	}

	public void setItems(Object items) {
		this.items = items;
	}

	public String getResultCode() {
		return resultCode;
	}

	public void setResultCode( String resultCode ) {
		this.resultCode = resultCode;
	}

	public String getResultMessage() {
		return resultMessage;
	}

	public void setResultMessage( String resultMessage ) {
		this.resultMessage = resultMessage;
	}

}