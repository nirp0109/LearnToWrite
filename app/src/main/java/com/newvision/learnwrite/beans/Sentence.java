package com.newvision.learnwrite.beans;

public class Sentence {

	private String sentence;//sentence as String
	private String feedBack;//path for the sound feedback as String
	private int _id=-1;
	
	
	public Sentence() {		
	}
	
	public Sentence(String sentence, String feedBack) {
		this.sentence = sentence;
		this.feedBack = feedBack;
	}


	public Sentence(String sentence, String feedBack, int _id) {		
		this.sentence = sentence;
		this.feedBack = feedBack;
		this._id = _id;
	}

	public String getSentence() {
		return sentence;
	}

	public void setSentence(String sentence) {
		this.sentence = sentence;
	}

	public String getFeedBack() {
		return feedBack;
	}

	public void setFeedBack(String feedBack) {
		this.feedBack = feedBack;
	}

	public int get_id() {
		return _id;
	}

	public void set_id(int _id) {
		this._id = _id;
	}

	@Override
	public String toString() {
		return sentence;
	}

	

}
