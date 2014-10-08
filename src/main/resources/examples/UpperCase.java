import java.util.Locale;

public class UpperCase
{
	public String upper;

	public UpperCase()
	{
		int iterations = 10_000_000;

		String source = "Lorem ipsum dolor sit amet, sensibus partiendo eam at.";

		long start = System.currentTimeMillis();
		convertString(source, iterations);
		System.out.println(upper);
		System.out.println(System.currentTimeMillis() - start);

		start = System.currentTimeMillis();
		convertCustom(source, iterations);
		System.out.println(upper);
		System.out.println(System.currentTimeMillis() - start);
	}

	private void convertString(String source, int iterations)
	{
		for (int i = 0; i < iterations; i++)
		{
			upper = source.toUpperCase(Locale.getDefault());
		}
	}

	private void convertCustom(String source, int iterations)
	{
		for (int i = 0; i < iterations; i++)
		{
			upper = doUpper(source);
		}
	}

	private String doUpper(String source)
	{
		StringBuilder builder = new StringBuilder();

		int len = source.length();

		for (int i = 0; i < len; i++)
		{
			char c = source.charAt(i);

			if (c >= 'a' && c <= 'z')
			{
				c -= 32;
			}

			builder.append(c);
		}

		return builder.toString();
	}

	public static void main(String[] args)
	{
		new UpperCase();
	}
}
