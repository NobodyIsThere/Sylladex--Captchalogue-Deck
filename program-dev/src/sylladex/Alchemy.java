package sylladex;

public final class Alchemy
{
	private final static char[] CHARS = { '0', '1', '2', '3', '4', '5', '6',
			'7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J',
			'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W',
			'X', 'Y', 'Z', 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j',
			'l', 'k', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w',
			'x', 'y', 'z', '?', '!' };
	
	public static enum Mode { AND, OR };
	
	/**
	 * Generates a CAPTCHA code from an item's name.
	 * @param string - The name of the item.
	 * @return The CAPTCHA code generated.
	 */
	public static String generateCode(String string)
	{
		if(string.length() < 3) { return string; }
		
		int value = Math.abs(string.hashCode());
			
		String code = toBase64(value);
		code = code.substring(0, 8);
		return code;
	}
	
	//Doesn't actually return the value in base 64. It'd be too short if it did.
	private static String toBase64(int hash)
	{
		String code = "";
		while(hash!=0)
		{
			int mod = hash % 64;
			code = CHARS[mod] + code;
			hash = hash/4;
		}
		return code;
	}
	
	/**
	 * Punch card alchemy go go go!
	 * 
	 * @param code1
	 *            8 character string using only letters of either case, numbers,
	 *            and ! or ?
	 * @param code2
	 *            8 character string using only letters of either case, numbers,
	 *            and ! or ?
	 * @param mode
	 *            Alchemy.Mode.AND or Alchemy.Mode.OR
	 * @return the final captcha string
	 */
	public static String calculateCaptcha(String code1, String code2, Mode mode)
	{
		String toreturn = "";
		
		int[] icode1 = new int[8];
		for(int i = 0; i < 8; i++)
		{
			int a = 0;
			for(int j = 0; j < 64; j++)
			{
				if(code1.charAt(i) == CHARS[j]) a = j;
			}
			icode1[i] = a;
		}
		
		int[] icode2 = new int[8];
		for(int i = 0; i < 8; i++)
		{
			int a = 0;
			for(int j = 0; j < 64; j++)
			{
				if(code2.charAt(i) == CHARS[j]) a = j;
			}
			icode2[i] = a;
		}
		
		int[] iFinal = new int[8];
		if(mode == Mode.AND)
			for(int i = 0; i < 8; i++)
				iFinal[i] = icode1[i] & icode2[i];
		else
			for(int i = 0; i < 8; i++)
				iFinal[i] = icode1[i] | icode2[i];
		for(int i = 0; i < 8; i++)
		{
			toreturn += CHARS[iFinal[i]];
		}
		return toreturn;
	}
	
	/**
	 * Punch card designix go go go!
	 * 
	 * @param code1
	 *            8 character string using only letters of either case, numbers,
	 *            and ! or ?
	 * @return a boolean[][], with true being punched.
	 */
	public static boolean[][] punchPattern(String code)
	{
		while (true)
		{
			if(code.equals("#Invalid#")) break;
			String tocard = "";
			int[] icode = new int[8];
			for(int i = 0; i < 8; i++)
			{
				int a = 0;
				for(int j = 0; j < 64; j++)
				{
					if(code.charAt(i) == CHARS[j]) a = j;
				}
				icode[i] = a;
			}
			for(int x : icode)
			{
				String toadd = Integer.toBinaryString(x);
				while (toadd.length() < 6)
				{
					toadd = 0 + toadd;
				}
				tocard += toadd;
			}
			boolean[][] toreturn = new boolean[4][12];
			int cardplacer = 0;
			for(int y = 0; y < 4; y++)
			{
				for(int x = 0; x < 12; x++)
				{
					if(Integer.parseInt(tocard.charAt(cardplacer) + "") == 1)
						toreturn[y][x] = true;
					else
						toreturn[y][x] = false;
					cardplacer++;
				}
			}
			return toreturn;
		}
		return new boolean[4][12];
	}
	
	/**
	 * Inverse punch card designix go go go!
	 * 
	 * @param card
	 *            a boolean[4][12], such as comes from punchPattern(), to find
	 *            the code of
	 * @return the8 character code that matches the card pattern
	 */
	public static String deriveCode(boolean[][] card)
	{
		while (true)
		{
			if(card.length != 4 && card[0].length != 12) break;
			String toreturn = "";
			String[] binarycode = new String[8];
			int bcplacer = 0;
			int bcaplacer = 0;
			for(int i = 0; i < 8; i++)
			{
				binarycode[i] = "";
			}
			for(int x = 0; x < 4; x++)
			{
				for(int y = 0; y < 12; y++)
				{
					if(card[x][y])
						binarycode[bcaplacer] += 1;
					else
						binarycode[bcaplacer] += 0;
					bcplacer++;
					if(bcplacer == 6)
					{
						bcaplacer++;
						bcplacer = 0;
					}
				}
			}
			for(int i = 0; i < 8; i++)
			{
				toreturn += CHARS[Integer.parseInt(binarycode[i], 2)];
			}
			return toreturn;
		}
		return "#Invalid#";
	}
}