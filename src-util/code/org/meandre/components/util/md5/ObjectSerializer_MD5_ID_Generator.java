/**
 * University of Illinois/NCSA
 * Open Source License
 *
 * Copyright (c) 2008, Board of Trustees-University of Illinois.
 * All rights reserved.
 *
 * Developed by:
 *
 * Automated Learning Group
 * National Center for Supercomputing Applications
 * http://www.seasr.org
 *
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to
 * deal with the Software without restriction, including without limitation the
 * rights to use, copy, modify, merge, publish, distribute, sublicense, and/or
 * sell copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 *  * Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimers.
 *
 *  * Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimers in the
 *    documentation and/or other materials provided with the distribution.
 *
 *  * Neither the names of Automated Learning Group, The National Center for
 *    Supercomputing Applications, or University of Illinois, nor the names of
 *    its contributors may be used to endorse or promote products derived from
 *    this Software without specific prior written permission.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.  IN NO EVENT SHALL THE
 * CONTRIBUTORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS
 * WITH THE SOFTWARE.
 */

package org.meandre.components.util.md5;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;
import java.security.MessageDigest;

import org.meandre.components.util.ByteArrayToHex;


/**
 * Takes any serializable object and generates a MD5 hash baed on the objects
 * serialized state.
 *
 * @author Amit Kumar
 * @author D. Searsmith
 *
 * TODO: Unit Tests
 */
public class ObjectSerializer_MD5_ID_Generator {

	static private MessageDigest md = null;

	private ObjectSerializer_MD5_ID_Generator() {
	}

	static public String getIdentifier(Object payload) throws Exception {
		if (md == null){
			md = MessageDigest.getInstance("MD5");
		}
		md.reset();
		byte[] barr = objectToSerializedByteArray(payload);
		md.update(barr, 0, barr.length);
		byte[] mdbytes = md.digest();
		String md5 = ByteArrayToHex.byteArrayToHex(mdbytes);
		return md5;
	}

	// write the object to the file if it already exists
	static private byte[] objectToSerializedByteArray(Object payload)
			throws Exception {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ObjectOutputStream oos = new ObjectOutputStream(
				new BufferedOutputStream(baos));
		oos.writeObject(payload);
		oos.flush();
		oos.close();
		return baos.toByteArray();
	}

}
