/*
 * The MIT License
 * 
 * Copyright (C) 2013 Kiyofumi Kondoh
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package jp.ne.sakura.kkkon.StripElfSectionHeader.ElfFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.security.InvalidParameterException;

import jp.ne.sakura.kkkon.StripElfSectionHeader.AppOption;

/**
 *
 * @author Kiyofumi Kondoh
 */
public class ElfFile
{
	protected byte[] _ident = new byte[IElfFile.EI_NINDENT];
	protected IElfFile _elfFile;

	public boolean readElfHeaderIdent( final RandomAccessFile input )
			throws IOException
	{
		if ( null == input )
		{
			throw new InvalidParameterException("input null");
		}

		final long current = input.getFilePointer();
		if ( 0 != current )
		{
			input.seek( 0 );
		}

		final int result = input.read( this._ident );
		if ( _ident.length != result )
		{
			return false;
		}

		return true;
	}
	
	public boolean writeElfHeaderIdent( final RandomAccessFile output )
			throws IOException
	{
		if ( null == output )
		{
			throw new InvalidParameterException("input null");
		}

		final long current = output.getFilePointer();
		if ( 0 != current )
		{
			output.seek( 0 );
		}

		output.write( this._ident );
		final long pos = output.getFilePointer();
		if ( this._ident.length != pos )
		{
			return false;
		}

		return true;
	}


    public static boolean isElfMagic( final byte[] buff )
    {
        if ( null == buff )
        {
            return false;
        }
        if ( buff.length < IElfFile.ELFMAGIC_COUNT )
        {
            return false;
        }
        
        if ( IElfFile.ELFMAGIC0 != buff[IElfFile.EI_MAGIC0] )
        {
            return false;
        }
        if ( IElfFile.ELFMAGIC1 != buff[IElfFile.EI_MAGIC1] )
        {
            return false;
        }
        if ( IElfFile.ELFMAGIC2 != buff[IElfFile.EI_MAGIC2] )
        {
            return false;
        }
        if ( IElfFile.ELFMAGIC3 != buff[IElfFile.EI_MAGIC3] )
        {
            return false;
        }
        
        return true;
    }
    
    public static boolean isElf32( final byte[] buff )
    {
        if ( null == buff )
        {
            return false;
        }
        if ( buff.length < IElfFile.EI_NINDENT )
        {
            return false;
        }
        
        if ( IElfFile.ELFCLASS32 != buff[IElfFile.EI_CLASS] )
        {
            return false;
        }
        
        return true;
    }

    public static boolean isElf64( final byte[] buff )
    {
        if ( null == buff )
        {
            return false;
        }
        if ( buff.length < IElfFile.EI_NINDENT )
        {
            return false;
        }
        
        if ( IElfFile.ELFCLASS64 != buff[IElfFile.EI_CLASS] )
        {
            return false;
        }
        
        return true;
    }

    public static boolean isElfLittleEndian( final byte[] buff )
    {
        if ( null == buff )
        {
            return false;
        }
        if ( buff.length < IElfFile.EI_NINDENT )
        {
            return false;
        }
        
        if ( IElfFile.ELFDATA2LSB != buff[IElfFile.EI_DATA] )
        {
            return false;
        }
        
        return true;
    }

    public static boolean isElfBigEndian( final byte[] buff )
    {
        if ( null == buff )
        {
            return false;
        }
        if ( buff.length < IElfFile.EI_NINDENT )
        {
            return false;
        }
        
        if ( IElfFile.ELFDATA2MSB != buff[IElfFile.EI_DATA] )
        {
            return false;
        }
        
        return true;
    }
    
    public static boolean isElfFile( final String path )
    {
        boolean isElfFile = false;

        File file = new File(path);
        {
            FileInputStream inStream = null;

            byte[] buff = new byte[IElfFile.ELFMAGIC_COUNT];
            try
            {
                inStream = new FileInputStream( file );
                final int nRet = inStream.read(buff);
                if ( IElfFile.ELFMAGIC_COUNT != nRet )
                {
                    isElfFile = false;
                }
                else
                {
                    isElfFile = isElfMagic( buff );
                }
            }
            catch ( FileNotFoundException e )
            {
                e.printStackTrace();
            }
            catch ( IOException e )
            {
                e.printStackTrace();
            }
            
            if ( null != inStream )
            {
                try { inStream.close(); } catch ( Exception e ) { }
            }
        }

        return isElfFile;
    }

    public boolean stripElfSectionHeader( final AppOption option, final String relativePath, final String path )
    {
        boolean isStripped = false;

        boolean isElfMagic = false;
        int ElfClass = IElfFile.ELFCLASSNONE;

        File file = new File(path);
        {
            FileInputStream inStream = null;

            byte[] buff = new byte[IElfFile.EI_NINDENT];
            try
            {
                inStream = new FileInputStream( file );
                final int nRet = inStream.read(buff);
                if ( IElfFile.EI_NINDENT != nRet )
                {
                    isStripped = false;
                }
                else
                {
                    if ( isElfMagic( buff ) )
                    {
                        isElfMagic = true;
                        if ( isElf32( buff ) )
                        {
                            ElfClass = IElfFile.ELFCLASS32;
                        }
                        else
                        if ( isElf64( buff ) )
                        {
                            ElfClass = IElfFile.ELFCLASS64;
                        }
                    }
                }
            }
            catch ( FileNotFoundException e )
            {
                e.printStackTrace();
            }
            catch ( IOException e )
            {
                e.printStackTrace();
            }
            
            if ( null != inStream )
            {
                try { inStream.close(); } catch ( Exception e ) { }
            }
        }

        if ( isElfMagic )
        {
            switch ( ElfClass )
            {
                case IElfFile.ELFCLASS32:
					_elfFile = new Elf32File();
					isStripped = ((Elf32File)_elfFile).stripSectionHeader( option, relativePath, path );
                    break;
                case IElfFile.ELFCLASS64:
                    System.err.println( "Not implemented ElfClass64" );
                    break;
                default:
                    System.err.println( "Unknown ElfClass" );
                    break;
            }
			
//			if ( null != _elfFile )
//			{
//				isStripped = _elfFile.stripSectionHeader( option, relativePath, path );
//			}
        }

        return isStripped;
    }
    

    static final int SHT_NULL = 0;
    static final int SHT_PROGBITS = 1;
    static final int SHT_SYMTAB = 2;
    static final int SHT_STRTAB = 3;
    static final int SHT_RELA = 4;
    static final int SHT_HASH = 5;
    static final int SHT_DYNAMIC = 6;
    static final int SHT_NOTE = 7;
    static final int SHT_NOBITS = 8;
    static final int SHT_REL = 9;
    static final int SHT_SHLIB = 10;
    static final int SHT_DYNSYM = 11;

    static final String ELF_BSS             = ".bss";
    static final String ELF_DATA            = ".data";
    static final String ELF_DEBUG           = ".debug";
    static final String ELF_DYNAMIC         = ".dynamic";
    static final String ELF_DYNSTR          = ".dynstr";
    static final String ELF_DYNSYM          = ".dynsym";
    static final String ELF_FINI            = ".fini";
    static final String ELF_GOT             = ".got";
    static final String ELF_HASH            = ".hash";
    static final String ELF_INIT            = ".init";
    static final String ELF_REL_DATA        = ".rel.data";
    static final String ELF_REL_FINI        = ".rel.fini";
    static final String ELF_REL_INIT        = ".rel.init";
    static final String ELF_REL_DYN         = ".rel.dyn";
    static final String ELF_REL_RODATA      = ".rel.rodata";
    static final String ELF_REL_TEXT        = ".rel.text";
    static final String ELF_RODATA          = ".rodata";
    static final String ELF_SHSTRTAB        = ".shstrtab";
    static final String ELF_STRTAB          = ".strtab";
    static final String ELF_SYMTAB          = ".symtab";
    static final String ELF_TEXT            = ".text";
    
}
