package com.hat.convert;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

/**
 * 1����apply plugin: 'android' �ĳ� apply plugin: 'com.android.application'
 * 2������android sdkĿ¼��ȡ��ǰ��compileSdkVersion �� buildToolsVersion �����滻
 * 3���жϵ�ǰ����Ŀ��gradle��app��gradle�������ǿ��
 * 4�������Ĺ��������jcenter
 * @author Administrator
 *
 */
public class convert {

	public static void main(String[] args) {
		
		if(args.length<2)
		{
			System.out.println("�����������sdk·�� ��Ŀ·��");
			return;
		}
//		dealMainGradle("D:/code/test/Android-PullToRefresh-master/build.gradle");
		String sdkPath = args[0];
		String projectPath = args[1];
		String compileVer = getCompileVer(sdkPath);
		String buildToolVer = getBuidlToolVersion(sdkPath);
		
		reaqdAllFolder(projectPath, compileVer, buildToolVer);
		
	}
	
	public static void reaqdAllFolder(String path, String compileVer, String toolVer)
	{
		File rootFile = new File(path);
		for(File file : rootFile.listFiles())
		{
			if(file.isDirectory())
				reaqdAllFolder(file.getAbsolutePath(), compileVer, toolVer);
			else if(file.getName().equals("build.gradle"))
				dealGradle(file.getAbsolutePath(), compileVer, toolVer);
			else if(file.getName().equals("gradle-wrapper.properties"))
				dealGradleWrapper(file.getAbsolutePath());
		}
	}
	
	public static void dealGradleWrapper(String path)
	{
		File file = new File(path);
		try {
			BufferedReader br = new BufferedReader(new FileReader(file));
			StringBuilder sb = new StringBuilder();
			String str = null;
			int type = -1; //0  ��Ŀgradle; 1: app grale 2: other gradle
			while((str = br.readLine()) != null)
			{
				if(str.contains("distributionUrl"))
					sb.append("distributionUrl=https\\://services.gradle.org/distributions/gradle-2.10-all.zip\r\n");
				else
					sb.append(str+"\r\n");
			}
			br.close();
			
			BufferedWriter bw = new BufferedWriter(new FileWriter(file));
			bw.write(sb.toString());
			bw.close();
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static void dealGradle(String path, String compileVer, String toolVer)
	{
		File file = new File(path);
		try {
			BufferedReader br = new BufferedReader(new FileReader(file));
			StringBuilder sb = new StringBuilder();
			String str = null;
			int type = -1; //0  ��Ŀgradle; 1: app grale 2: other gradle
			while((str = br.readLine()) != null)
			{
				if(str.contains("'com.android.application'" ) || str.contains("'android'"))
					type = 1;
				else if(str.contains("'com.android.library'") || str.contains("'android-library'"))
					type = 2;
				else
				{
					if(type == -1)
						type = 0;
				}
				
				if(type == 0)
				{
					if(str.contains("com.android.tools.build:gradle"))
					{
						sb.append("classpath 'com.android.tools.build:gradle:2.1.0'\r\n");
						continue;
					}
				}
				else if(type ==1 )
				{
					if(str.contains("'android'"))
					{
						sb.append(str.replace("'android'", "'com.android.application'") + "\r\n");
						continue;
					}
				}
				else
				{
					if(str.contains("'android-library'"))
					{
						sb.append(str.replace("'android-library'", "'com.android.library'") + "\r\n");
						continue;
					}
				}
				if(str.contains("compileSdkVersion"))
					sb.append("compileSdkVersion " + compileVer + "\r\n");
				else if(str.contains("buildToolsVersion"))
						sb.append("buildToolsVersion '" + toolVer + "'\r\n");
				else
					sb.append(str + "\r\n");
			}
			
			String deal1Str = sb.toString().toString().replace("mavenCentral", "jcenter");
			if(type == 0 && !deal1Str.contains("allprojects"))
				deal1Str = deal1Str + "\r\n" + "allprojects {\r\n\trepositories {\r\n\t\tjcenter()\r\n\t }\r\n}";
			else if(type == 1 && !deal1Str.contains("buildscript"))
				deal1Str = deal1Str + "buildscript {\r\n"
						+ "\trepositories {\r\n"
						+ "\t\tjcenter()\r\n"
						+ "\t}\r\n"
						+ "\tdependencies {\r\n"
						+ "\t\tclasspath 'com.android.tools.build:gradle:2.1.0'\r\n"
						+ "\t}\r\n"
						+ "}\r\n";

			br.close();
			
			BufferedWriter bw = new BufferedWriter(new FileWriter(file));
			bw.write(deal1Str);
			bw.close();
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	/**
	 * ��ȡcompile�汾
	 * @param sdkPath
	 * @return
	 */
	public static String getCompileVer(String sdkPath)
	{
		String path = sdkPath + "/platforms";
		File folder = new File(path);
		int maxVer = 12;
		for(File file : folder.listFiles())
		{
			if(file.isDirectory())
			{
				int tmpInt = Integer.parseInt(file.getName().replace("android-", ""));
				if(maxVer < tmpInt)
					maxVer = tmpInt;
			}
		}
		return maxVer + "";
	}
	
	/**
	 * �Ƚϰ汾
	 * @param ver1
	 * @param ver2
	 * @return
	 */
	private static String getMaxVer(String ver1, String ver2)
	{
		String[] str1 = ver1.split("\\.");
		String[] str2 = ver2.split("\\.");
		if(ver1.length() == 0)
			return ver2;
		
		if(ver2.length() == 0)
			return ver1;
		
		int maxLen = str1.length > str2.length ? str2.length : str1.length;
		for(int i=0;i <maxLen; i++)
		{
			if(Integer.parseInt(str1[i]) > Integer.parseInt(str2[i]))
				return ver1;
			else if(Integer.parseInt(str1[i]) < Integer.parseInt(str2[i]))
				return ver2;
		}
		return ver1;
	}
	
	/**
	 * ��ȡ����build tool version
	 * @param sdkPath
	 * @return
	 */
	public static String getBuidlToolVersion(String sdkPath)
	{
		String path = sdkPath + "/build-tools";
		File folder = new File(path);
		String maxVer = "";
		for(File file : folder.listFiles())
		{
			if(file.isDirectory())
			{
				maxVer = getMaxVer(maxVer, file.getName());
			}
		}
		return maxVer;
	}
}
