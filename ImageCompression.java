
package ImageCompression;


import java.io.BufferedOutputStream;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Scanner;

import Jama.Matrix;
import Jama.SingularValueDecomposition;

public class ImageCompression {

    static int height = 0, width = 0, max_pixel_value;
    public static String fileName;
    public static ArrayList<ArrayList<Integer>> pixelV;

    public static void main(String a[]) throws IOException {
        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
        int user_choice = Integer.parseInt(a[0]);
        switch (user_choice) {
            case 1:
                pixelV = new ArrayList<ArrayList<Integer>>();
                System.out.println(a[1]);
                File fp = new File(a[1]); 
                fileName = a[1];
                Scanner sc = new Scanner(fp);
                long startTime = System.currentTimeMillis();
                sc.nextLine();
                sc.nextLine();
                width = Integer.parseInt(sc.next());
                height = Integer.parseInt(sc.next());
                max_pixel_value = Integer.parseInt(sc.next());
                String entries = "";
                for (int i1 = 0; i1 < height; i1++) {
                    ArrayList<Integer> t = new ArrayList<Integer>();
                    for (int j1 = 0; j1 < width; j1++) {
                        t.add(Integer.parseInt(sc.next()));
                    }
                    pixelV.add(t);
                }
                DataOutputStream dos = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(fileName.substring(0, fileName.indexOf(".")) + "_b" + ".pm")));
                String width_binary = binary_2_convert(width);
                String height_binary = binary_2_convert(height);
                int b1 = integer_value(width_binary.substring(0, 8));
                int b2 = integer_value(width_binary.substring(8, 16));
                int h1 = integer_value(height_binary.substring(0, 8));
                int h2 = integer_value(height_binary.substring(8, 16));
                dos.writeByte(b1);
                dos.writeByte(b2);
                dos.writeByte(h1);
                dos.writeByte(h2);
                dos.writeByte(max_pixel_value);
                for (ArrayList<Integer> a1 : pixelV) {
                    for (int i = 0; i < a1.size(); i++) {
                    dos.writeByte(a1.get(i));
                    }
                }
              ///  System.out.println("inside a1"+ a[1]);
                createSVD(fileName);
                dos.flush();
                dos.close();
                break;
            case 2:
                BufferedReader image_reader1 = new BufferedReader(new FileReader(a[1]));
                DataInputStream dis = new DataInputStream(new BufferedInputStream(new FileInputStream(a[1])));
                pixelV = new ArrayList<ArrayList<Integer>>();
                fileName = a[1];
                try {
                    PrintWriter pw = new PrintWriter(fileName.substring(0,
                            fileName.indexOf("."))
                            + "_2" + ".pm");
                    pw.println("P2");
                    pw.println("# Created by IrfanView");
                    byte hw[] = new byte[5];
                    dis.read(hw);
                    width = integer_value(binary_1_convert(byte2Int(hw[0])) + binary_1_convert(byte2Int(hw[1])));
                    height = integer_value(binary_1_convert(byte2Int(hw[2])) + binary_1_convert(byte2Int(hw[3])));

                    max_pixel_value = byte2Int(hw[4]);
                    //System.out.println(width+"   "+height+"   "+max_pixel_value);
                    pw.write(width + " ");
                    pw.write(height + "\n");
                    pw.write(max_pixel_value + "\n");
                    byte t[] = new byte[width * height];
                    dis.read(t);
                    int tij = 0;
                    for (int ti = 0; ti < height; ti++) {
                        for (int tj = 0; tj < width; tj++) {
                            int pix;
                            if ((new Byte(t[tij])).intValue() < 0) {
                                pix = 128 + (127 + (new Byte(t[tij])).intValue() + 1);
                            } else {
                                pix = (new Byte(t[tij])).intValue();
                            }
                            pw.write(pix + " ");
                            tij++;
                        }
                        pw.write("\n");
                    }
                    pw.close();
                } catch (FileNotFoundException e) {
                    System.out.println("Cannot create op file");
                }
                break;
            case 3:
                String headerFilename = a[1];
                String svdFilename = a[2];
                int k = Integer.parseInt(a[3]);
                SVD Svd2 = new SVD();
                Svd2.readHeader(headerFilename);
                Svd2.readText(svdFilename);
                Svd2.setRank(k);
               // System.out.println("Relative error: " + Svd2.getRelativeError());
                Svd2.writeBinary("image_b.pm.SVD");
                break;
            case 4:
                String sourceFilename = a[1];
                SVD mySvd2 = new SVD();
                mySvd2.readBinary(sourceFilename);
                Matrix u = mySvd2.getU();
                Matrix s = mySvd2.getS();
                Matrix v = mySvd2.getV();
                Matrix restore = u.times(s.times(v.transpose()));
                if (mySvd2.isTransposed()) // Should use EJML to prevent transpose
                {
                    restore = restore.transpose();
                }
                int[][] gray_values= new int[restore.getRowDimension()][restore.getColumnDimension()];
                int n = 0;
                for (int i = 0; i < gray_values.length; i++) {
                    for (int j = 0; j < gray_values[i].length; j++) {
                        n = (int) restore.get(i, j);
                        if (n < 0) {
                            gray_values[i][j] = 0;
                        } else if (n > 255) {
                            gray_values[i][j] = 255;
                        } else {
                            gray_values[i][j] = n;
                        }
                    }
                }
                Pgm pm1 = new Pgm();
                pm1.setHeight(gray_values.length);
                pm1.setWidth(gray_values[0].length);
                pm1.setMaxval(255);
                pm1.setGrays(gray_values);
                String fn = sourceFilename.split("_b\\.pm\\.")[0];
                String tragetFilename = fn + "_k.pm";
                pm1.writePgm(tragetFilename);
                break;
            default:
                System.out.println("Incorrect Choice Please try Again");
                break;
        }
    }

    public static void createSVD(String src) {
        Pgm pm = new Pgm();
        pm.readPgm(src);
        int[][] gray_values = pm.getGrays();
        
        Matrix m = new Matrix(pm.getHeight(), pm.getWidth());
        for (int i = 0; i < pm.getHeight(); i++) {
            for (int j = 0; j < pm.getWidth(); j++) {
                m.set(i, j, (double) gray_values[i][j]);
            }
        }
        if (pm.getWidth() > pm.getHeight()) {
            m = m.transpose();
        }

        SingularValueDecomposition svd = m.svd();
        SVD Svd2;
        Svd2 = new SVD();
        Svd2.setU(svd.getU());
        Svd2.setS(svd.getS());
        Svd2.setV(svd.getV());
        Svd2.writeText("SVD.txt");

    }

    public static String binary_1_convert(int a) {
        String x = Integer.toBinaryString(a);
        if (x.length() <= 8) {
            for (int i = x.length(); i < 8; i++) {
                x = "0" + x;
            }
        }
        return x;
    }

    public static String binary_2_convert(int a) {
        String x = Integer.toBinaryString(a);
        if (x.length() <= 16) {
            for (int i = x.length(); i < 16; i++) {
                x = "0" + x;
            }
        }
        return x;
    }

    public static int integer_value(String b) {
        int i = 0;
        StringBuffer b1 = new StringBuffer(b);
        b1.reverse();
        //System.out.println(b1);
        for (int i1 = 0; i1 < b1.length(); i1++) {
            if (b1.charAt(i1) == '1') {
                i += Math.pow(2, i1);
            }
        }
        return i;
    }

    public static int byte2Int(Byte b) {
        if (b.intValue() < 0) {
            return 128 + (127 + b.intValue() + 1);
        } else {
            return b.intValue();
        }
    }

    public static String binary_3_convert(int a) {
        String x = Integer.toBinaryString(a);
        if (x.length() <= 24) {
            for (int i = x.length(); i < 24; i++) {
                x = "0" + x;
            }
        }
        return x;
    }
}
 class SVD
	{
	   public void readText(String fn)
	   {
	      try
	      {
	         Scanner scanner = new Scanner(new File(fn));
	         for (int i = 0; i < u.getRowDimension(); i++)
	         {
	            for (int j = 0; j < u.getColumnDimension(); j++)
	            {
	               u.set(i, j, scanner.nextDouble());
	            }
	         }
	         for (int i = 0; i < s.getRowDimension(); i++)
	         {
	            for (int j = 0; j < s.getColumnDimension(); j++)
	            {
	               s.set(i, j, scanner.nextDouble());
	            }
	         }
	         for (int i = 0; i < v.getRowDimension(); i++)
	         {
	            for (int j = 0; j < v.getColumnDimension(); j++)
	            {
	               v.set(i, j, scanner.nextDouble());
	            }
	         }
	         scanner.close();
	      }
	      catch (Exception e)
	      {
	         e.printStackTrace();
	      }
	   }

	   /**
	    * Write three matrices to a text SVD file.
	    * 
	    * @param fn
	    *           fn of the SVD file.
	    */
	   public void writeText(String fn)
	   {
	      try
	      {
	         PrintWriter op = new PrintWriter(fn);
	         for (int i = 0; i < u.getRowDimension(); i++)
	         {
	            for (int j = 0; j < v.getRowDimension(); j++)
	            {
	               op.print(u.get(i, j) + " ");
	            }
	         }
	         for (int i = 0; i < v.getRowDimension(); i++)
	         {
	            for (int j = 0; j < v.getRowDimension(); j++)
	            {
	               op.print(s.get(i, j) + " ");
	            }
	         }
	         for (int i = 0; i < v.getRowDimension(); i++)
	         {
	            for (int j = 0; j < v.getRowDimension(); j++)
	            {
	               op.print(v.get(i, j) + " ");
	            }
	         }
	         op.close();
	      }
	      catch (Exception e)
	      {
	         e.printStackTrace();
	      }
	   }

	   public void readBinary(String fn)
	   {
	      try
	      {
	         DataInputStream ip = new DataInputStream(
	               new FileInputStream(fn));
	         if (ip.readBoolean() == true)
	         {
	            transposed = true;
	         }
	         else
	         {
	            transposed = false;
	         }
	         int urd = ip.readShort(); // First number is row dimension
	         int vrd = ip.readShort(); // Second is column dimension
	         rank = ip.readShort();              // Third number is rank
	         u = new Matrix(urd, rank);
	         s = new Matrix(rank, rank);
	         v = new Matrix(vrd, rank);
	         for (int i = 0; i < urd; i++)
	         {
	            for (int j = 0; j < rank; j++)
	            {
	               u.set(i, j, ip.readShort() / FACTOR);
	            }
	         }
                 
	         for (int i = 0; i < rank; i++)
	         {
	            for (int j = 0; j < rank; j++)
	            {
	               if (i == j)
	               {
	                  s.set(i, j, ip.readFloat());
	               }
	               else
	               {
	                  s.set(i, j, 0.0);
	               }
	            }
	         }
	         for (int i = 0; i < vrd; i++)
	         {
	            for (int j = 0; j < rank; j++)
	            {
	               v.set(i, j, ip.readShort() / FACTOR);
	            }
	         }
	         ip.close();
	      }
	      catch (Exception e)
	      {
	         e.printStackTrace();
	      }
	   }

	   public void writeBinary(String fn)
	   {
	      try
	      {
	         DataOutputStream op = new DataOutputStream(new FileOutputStream(
	               fn));
	         if (transposed)
	         {
	            op.writeBoolean(true);
	         }
	         else
	         {
	            op.writeBoolean(false);
	         }
	         op.writeShort(u.getRowDimension());     // height
	         op.writeShort(v.getColumnDimension());  // width
	         op.writeShort(rank);                    // rank
	         for (int i = 0; i < u.getRowDimension(); i++)
	         {
	            for (int j = 0; j < rank; j++)
	            {
	               op.writeShort((short) (u.get(i, j) * FACTOR));
	            }
	         }
	         for (int i = 0; i < rank; i++)
	         {
	            op.writeFloat((float) (s.get(i, i)));
	         }
	         for (int i = 0; i < v.getRowDimension(); i++)
	         {
	            for (int j = 0; j < rank; j++)
	            {
	               op.writeShort((short) (v.get(i, j) * FACTOR));
	            }
	         }
	         op.close();
	      }
	      catch (Exception e)
	      {
	         e.printStackTrace();
	      }
	   }

	   public void readHeader(String fn)
	   {
	      try
	      {
	         Scanner scanner = new Scanner(new File(fn));
	         int width = scanner.nextInt();
	         int height = scanner.nextInt();
	         if (width > height)
	         {
	            transposed = true;
	            u = new Matrix(width, height);
	            s = new Matrix(height, height);
	            v = new Matrix(height, height);
	         }
	         else
	         {
	            transposed = false;
	            u = new Matrix(height, width);
	            s = new Matrix(width, width);
	            v = new Matrix(width, width);
	         }

	         scanner.close();
	      }
	      catch (Exception e)
	      {
	         e.printStackTrace();
	      }
	   }

	   public double getRelativeError()
	   {
	      int size = Math.min(u.getRowDimension(), v.getRowDimension());

	      double norm1 = 0;
	      for (int i = 0; i < size; i++)
	      {
	         norm1 += s.get(i, i) * s.get(i, i);
	      }

	      double norm2 = 0;
	      for (int i = rank; i < size; i++)
	      {
	         norm2 += s.get(i, i) * s.get(i, i);
	      }
	      return Math.sqrt(norm2 / norm1);
	   }

	   public Matrix getU()
	   {
	      return u;
	   }

	   public void setU(Matrix u)
	   {
	      this.u = u;
	   }

	   public Matrix getS()
	   {
	      return s;
	   }

	   public void setS(Matrix s)
	   {
	      this.s = s;
	   }

	   public Matrix getV()
	   {
	      return v;
	   }

	   public void setV(Matrix v)
	   {
	      this.v = v;
	   }

	   public int getRank()
	   {
	      return rank;
	   }

	   public void setRank(int rank)
	   {
	      this.rank = rank;
	   }

	   public boolean isTransposed()
	   {
	      return transposed;
	   }

	   public void setTransposed(boolean transposed)
	   {
	      this.transposed = transposed;
	   }

	   private Matrix u;
	   private Matrix s;
	   private Matrix v;
	   private int rank;
	   private boolean transposed;
	   private final double FACTOR = 32768.0;
	}





class Pgm {

    static int width, height, gray_values[][], maxval;

    public static void main1() {

    }

    public int[][] getGrays() {
        return gray_values;
    }

    public int getHeight() {
        return height;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public void setMaxval(int maxval) {
        this.maxval = maxval;
    }

    public void setGrays(int[][] gray_values) {
        this.gray_values = gray_values;
    }

    public int getWidth() {
        return width;
    }

    public void writePgm(String fn) {
        try {
            PrintWriter op = new PrintWriter(fn);
            op.println("P2");
            op.println(width + " " + height);
            op.println(maxval);

            int i = 0;
            while (i < height * width) {
                op.print(gray_values[i / width][i % width] + " ");
                if ((i + 1) % 16 == 0) {
                    op.println();
                }
                i++;
            }
            op.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void readPgm(String fn) {
        try {
            Scanner scanner = new Scanner(new File(fn));
            scanner.nextLine();
               scanner.nextLine();
                
            width = Integer.parseInt(scanner.next());
                height = Integer.parseInt(scanner.next());
               // line = scanner.nextLine().trim();
            maxval = Integer.parseInt(scanner.next());
            //System.out.println("wid="+width+"hie="+height);
            gray_values = new int[height][width];

            int i = 0;
            while (scanner.hasNextInt()) {
                gray_values[i / width][i % width] = scanner.nextInt();
                i++;
            }
            scanner.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
