package data;

import java.io.Serializable;
import java.util.Vector;

public class CandidateAnswer implements Serializable 
{	
	private static final long serialVersionUID = 1L;
	
	private Long			questionID;
	private Vector<Double>	features;
	private boolean			isCorrect;
	
	public CandidateAnswer(Long qID, Vector<Double> features)
	{
		this.questionID = qID;
		this.features = features;
	}
	public CandidateAnswer(Long qID, Vector<Double> features, boolean isCorrect)
	{
		this.questionID = qID;
		this.features = features;
		this.isCorrect = isCorrect;
	}
	
	public Long getQuestionID()
	{
		return this.questionID;
	}
	
	public Double getFeature(int i)
	{
		return this.features.get(i);
	}
	
	public void setFeature(int i, Double x)
	{
		this.features.set(i, x);
	}
	
	public boolean isCorrect()
	{
		return this.isCorrect;
	}
	public void isCorrect(boolean isCorrect)
	{
		this.isCorrect = isCorrect;
	}
}
