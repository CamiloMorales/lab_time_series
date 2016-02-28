package edu.unibonn.distances.histogram;

public class RepeatedNumber implements Comparable<RepeatedNumber>
{
	private float number;
	private int repetitions;
	
	public RepeatedNumber(float number)
	{
		this.number = number;
		this.repetitions = 1;
	}

	public float getNumber() {
		return number;
	}

	public void setNumber(float number) {
		this.number = number;
	}

	public int getRepetitions() {
		return repetitions;
	}

	public void setRepetitions(int repetitions) {
		this.repetitions = repetitions;
	}
	
	public boolean equals(Object o) 
	{
        if (!(o instanceof RepeatedNumber))
        {
        	return false;
        }
            
        RepeatedNumber n = (RepeatedNumber) o;
        return n.number == this.number;
    }

    public int compareTo(RepeatedNumber n) 
    {
        return Float.compare(this.number, n.number);
    }

	public void addRepetition()
	{
		this.repetitions++;
	}
	
	@Override
	public String toString()
	{
		return this.number+","+this.repetitions;
	}

	public void normalize()
	{
		this.repetitions = this.repetitions/2;
	}
}
