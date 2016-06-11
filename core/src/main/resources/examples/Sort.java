import java.util.Arrays;
import java.util.Random;

public class Sort
{
	public static final int ARRAY_LENGTH = 1_000_000;
	public double[] doubleArray1 = new double[ARRAY_LENGTH];
	public double[] doubleArray2 = new double[ARRAY_LENGTH];

	public Sort()
	{
		fillArrays();

		long count1 = countGreaterThanHalfWithSort(doubleArray1);
		long count2 = countGreaterThanHalfWithoutSort(doubleArray2);

		System.out.println(count1);
		System.out.println(count2);
	}

	private void fillArrays()
	{
		Random random = new Random(19762211);

		for (int i = 0; i < ARRAY_LENGTH; i++)
		{
			double next = random.nextDouble();

			doubleArray1[i] = next;
			doubleArray2[i] = next;
		}
	}

	private long countGreaterThanHalfWithSort(double[] theDoubles)
	{
		long count = 0;

		Arrays.sort(theDoubles);

		for (int i = 0; i < ARRAY_LENGTH; i++)
		{
			if (theDoubles[i] > 0.5)
			{
				count++;
			}
		}
		return count;
	}

	private long countGreaterThanHalfWithoutSort(double[] theDoubles)
	{
		long count = 0;

		for (int i = 0; i < ARRAY_LENGTH; i++)
		{
			if (theDoubles[i] > 0.5)
			{
				count++;
			}
		}
		return count;
	}

	public static void main(String[] args)
	{
		new Sort();
	}
}