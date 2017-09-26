
public class LoopUnroll
{
	private static final int MAX = 1_000_000;

	private long[] data = new long[MAX];

	public LoopUnroll()
	{
		createData();

		long total = 0;

		total += constantStride1Int();
		total += constantStride2Int();
		total += constantStride4Int();
		total += constantStride8Int();
		total += constantStride16Int();
		total += variableStrideInt(1);
		total += constantStride1Long();
		total += constantStride1IntWithExit();
		total += constantStride1IntWith2Exits();
		total += constantStride1IntWith4Exits();

		System.out.println("Total: " + total);
	}

	private void createData()
	{
		java.util.Random random = new java.util.Random();

		for (int i = 0; i < MAX; i++)
		{
			data[i] = random.nextLong();
		}
	}

	private long constantStride1Int()
	{
		long sum = 0;
		for (int i = 0; i < MAX; i += 1)
		{
			sum += data[i];
		}
		return sum;
	}

	private long constantStride2Int()
	{
		long sum = 0;
		for (int i = 0; i < MAX; i += 2)
		{
			sum += data[i];
		}
		return sum;
	}

	private long constantStride4Int()
	{
		long sum = 0;
		for (int i = 0; i < MAX; i += 4)
		{
			sum += data[i];
		}
		return sum;
	}

	private long constantStride8Int()
	{
		long sum = 0;
		for (int i = 0; i < MAX; i += 8)
		{
			sum += data[i];
		}
		return sum;
	}

	private long constantStride16Int()
	{
		long sum = 0;
		for (int i = 0; i < MAX; i += 16)
		{
			sum += data[i];
		}
		return sum;
	}

	private long variableStrideInt(int stride)
	{
		long sum = 0;
		for (int i = 0; i < MAX; i += stride)
		{
			sum += data[i];
		}
		return sum;
	}

	private long constantStride1Long()
	{
		long sum = 0;
		for (long l = 0; l < MAX; l++)
		{
			// cast because array index can't be longer than int
			sum += data[(int) l];
		}
		return sum;
	}

	// NICE !!! unrolls with test on each unroll :)
	private long constantStride1IntWithExit()
	{
		long sum = 0;

		for (int i = 0; i < MAX; i += 1)
		{
			if (data[i] == 0x1234)
			{
				break;
			}
			else
			{
				sum += data[i];
			}
		}

		return sum;
	}

	// NICE !!! unrolls with test on each unroll :)
	private long constantStride1IntWith2Exits()
	{
		long sum = 0;

		for (int i = 0; i < MAX; i += 1)
		{
			if (data[i] == 0x1234)
			{
				break;
			}
			else if (data[i] == 0x5678)
			{
				break;
			}
			else
			{
				sum += data[i];
			}
		}

		return sum;
	}

	// NICE !!! unrolls with test on each unroll :)
	private long constantStride1IntWith4Exits()
	{
		long sum = 0;

		for (int i = 0; i < MAX; i += 1)
		{
			if (data[i] == 0x1234)
			{
				break;
			}
			else if (data[i] == 0x5678)
			{
				break;
			}
			else if (data[i] == 0x9ABC)
			{
				break;
			}
			else if (data[i] == 0xDEF0)
			{
				break;
			}
			else
			{
				sum += data[i];
			}
		}

		return sum;
	}

	public static void main(String[] args)
	{
		new LoopUnroll();
	}
}
