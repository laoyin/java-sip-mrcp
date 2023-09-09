/*
    This file is part of Peers, a java SIP softphone.

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.

    Copyright 2010 Oleg Kulikov, Yohann Martineau
*/

package com.mrcp.yxp.protocol.peers.media;

public class PcmuDecoder extends Decoder {

//    private final static int cBias = 0x84;
//    private int QUANT_MASK = 0xf;
//    private final static int SEG_SHIFT = 4;
//    private final static int SEG_MASK = 0x70;
//    private final static int SIGN_BIT = 0x80;
    private static short muLawDecompressTable[] = new short[]{
        -32124, -31100, -30076, -29052, -28028, -27004, -25980, -24956,
        -23932, -22908, -21884, -20860, -19836, -18812, -17788, -16764,
        -15996, -15484, -14972, -14460, -13948, -13436, -12924, -12412,
        -11900, -11388, -10876, -10364, -9852, -9340, -8828, -8316,
        -7932, -7676, -7420, -7164, -6908, -6652, -6396, -6140,
        -5884, -5628, -5372, -5116, -4860, -4604, -4348, -4092,
        -3900, -3772, -3644, -3516, -3388, -3260, -3132, -3004,
        -2876, -2748, -2620, -2492, -2364, -2236, -2108, -1980,
        -1884, -1820, -1756, -1692, -1628, -1564, -1500, -1436,
        -1372, -1308, -1244, -1180, -1116, -1052, -988, -924,
        -876, -844, -812, -780, -748, -716, -684, -652,
        -620, -588, -556, -524, -492, -460, -428, -396,
        -372, -356, -340, -324, -308, -292, -276, -260,
        -244, -228, -212, -196, -180, -164, -148, -132,
        -120, -112, -104, -96, -88, -80, -72, -64,
        -56, -48, -40, -32, -24, -16, -8, 0,
        32124, 31100, 30076, 29052, 28028, 27004, 25980, 24956,
        23932, 22908, 21884, 20860, 19836, 18812, 17788, 16764,
        15996, 15484, 14972, 14460, 13948, 13436, 12924, 12412,
        11900, 11388, 10876, 10364, 9852, 9340, 8828, 8316,
        7932, 7676, 7420, 7164, 6908, 6652, 6396, 6140,
        5884, 5628, 5372, 5116, 4860, 4604, 4348, 4092,
        3900, 3772, 3644, 3516, 3388, 3260, 3132, 3004,
        2876, 2748, 2620, 2492, 2364, 2236, 2108, 1980,
        1884, 1820, 1756, 1692, 1628, 1564, 1500, 1436,
        1372, 1308, 1244, 1180, 1116, 1052, 988, 924,
        876, 844, 812, 780, 748, 716, 684, 652,
        620, 588, 556, 524, 492, 460, 428, 396,
        372, 356, 340, 324, 308, 292, 276, 260,
        244, 228, 212, 196, 180, 164, 148, 132,
        120, 112, 104, 96, 88, 80, 72, 64,
        56, 48, 40, 32, 24, 16, 8, 0
    };

    @Override
    public byte[] process(byte[] media) {
        byte[] res = new byte[media.length * 2];
        int j = 0;
        for (int i = 0; i < media.length; i++) {
            short s = muLawDecompressTable[media[i] & 0xff];
            res[j++] = (byte) s;
            res[j++] = (byte) (s >> 8);
        }
        return res;
    }

    //TODO compare what's the fastest: table lookup or real conversion
    /*
     * ulaw2linear() - Convert a u-law value to 16-bit linear PCM
     *
     * First, a biased linear code is derived from the code word. An unbiased
     * output can then be obtained by subtracting 33 from the biased code.
     *
     * Note that this function expects to be passed the complement of the
     * original code word. This is in keeping with ISDN conventions.
     */
//    private short ulaw2linear(byte u_val) {
//        int t;
//
//        /* Complement to obtain normal u-law value. */
//        u_val = (byte) ~u_val;
//
//        /*
//         * Extract and bias the quantization bits. Then
//         * shift up by the segment number and subtract out the bias.
//         */
//        t = ((u_val & QUANT_MASK) << 3) + cBias;
//        t <<= (u_val & SEG_MASK) >> SEG_SHIFT;
//
//        boolean s = (u_val & SIGN_BIT) == SIGN_BIT;
//        return (short) (s ? (cBias - t) : (t - cBias));
//    }

}
