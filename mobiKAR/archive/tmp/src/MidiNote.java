/*
 *	MidiNote.java
 *
 *	This file is part of jsresources.org
 */

/*
 * Copyright (c) 1999 - 2001 by Matthias Pfisterer
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * - Redistributions of source code must retain the above copyright notice,
 *   this list of conditions and the following disclaimer.
 * - Redistributions in binary form must reproduce the above copyright
 *   notice, this list of conditions and the following disclaimer in the
 *   documentation and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS
 * FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE
 * COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 */

/*
|<---            this code is formatted to fit into 80 columns             --->|
*/

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiDevice;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.Receiver;
import javax.sound.midi.MidiMessage;
import javax.sound.midi.ShortMessage;
import javax.sound.midi.SysexMessage;


// TODO: an optional delay parameter that is added to getMicrosecondPosition to be used as timestamp for the event delivery.

/**	<titleabbrev>MidiNote</titleabbrev>
	<title>Playing a note on a MIDI device</title>

	<formalpara><title>Purpose</title>
	<para>Plays a single note on a MIDI device. The MIDI device can
	be a software synthesizer, an internal hardware synthesizer or
	any device connected to the MIDI OUT port.</para>
	</formalpara>

	<formalpara><title>Usage</title>
	<para>
	<cmdsynopsis><command>java MidiNote</command>
	<arg choice="plain"><replaceable class="parameter">keynumber</replaceable></arg>
	<arg choice="plain"><replaceable class="parameter">velocity</replaceable></arg>
	<arg choice="plain"><replaceable class="parameter">duration</replaceable></arg>
	</cmdsynopsis>
	</para></formalpara>

	<formalpara><title>Parameters</title>
	<variablelist>
	<varlistentry>
	<term><replaceable class="parameter">keynumber</replaceable></term>
	<listitem><para>the MIDI key number</para></listitem>
	</varlistentry>
	<varlistentry>
	<term><replaceable class="parameter">velocity</replaceable></term>
	<listitem><para>the velocity</para></listitem>
	</varlistentry>
	<varlistentry>
	<term><replaceable class="parameter">duration</replaceable></term>
	<listitem><para>the duration in milliseconds</para></listitem>
	</varlistentry>
	</variablelist>
	</formalpara>

	<formalpara><title>Bugs, limitations</title>
	<para>Not well-tested.</para>
	</formalpara>

	<formalpara><title>Source code</title>
	<para>
	<ulink url="MidiNote.java.html">MidiNote.java</ulink>,
	<ulink url="MidiCommon.java.html">MidiCommon.java</ulink>
	</para>
	</formalpara>

*/
public class MidiNote
{
	/**	Flag for debugging messages.
	 	If true, some messages are dumped to the console
	 	during operation.
	*/
	private static boolean		DEBUG = true;



	public static void main(String[] args)
	{
		// TODO: make settable via command line
		int	nChannel = 0;

		int	nKey = 0;	// MIDI key number
		int	nVelocity = 0;

		/*
		 *	Time between note on and note off event in
		 *	milliseconds. Note that on most systems, the
		 *	best resolution you can expect are 10 ms.
		 */
		int	nDuration = 0;
		int	nArgumentIndexOffset = 0;
		String	strDeviceName = null;
		if (args.length == 4)
		{
			strDeviceName = args[0];
			nArgumentIndexOffset = 1;
		}
		else if (args.length == 3)
		{
			nArgumentIndexOffset = 0;
		}
		else
		{
			printUsageAndExit();
		}
		nKey = Integer.parseInt(args[0 + nArgumentIndexOffset]);
		nKey = Math.min(127, Math.max(0, nKey));
		nVelocity = Integer.parseInt(args[1 + nArgumentIndexOffset]);
		nVelocity = Math.min(127, Math.max(0, nVelocity));
		nDuration = Integer.parseInt(args[2 + nArgumentIndexOffset]);
		nDuration = Math.max(0, nDuration);


		MidiDevice	outputDevice = null;
		Receiver	receiver = null;
		if (strDeviceName != null)
		{
			MidiDevice.Info	info = MidiCommon.getMidiDeviceInfo(strDeviceName, true);
			if (info == null)
			{
				out("no device info found for name " + strDeviceName);
				System.exit(1);
			}
			try
			{
				outputDevice = MidiSystem.getMidiDevice(info);
				if (DEBUG) out("MidiDevice: " + outputDevice);
				outputDevice.open();
			}
			catch (MidiUnavailableException e)
			{
				if (DEBUG) out(e);
			}
			if (outputDevice == null)
			{
				out("wasn't able to retrieve MidiDevice");
				System.exit(1);
			}
			try
			{
				receiver = outputDevice.getReceiver();
			}
			catch (MidiUnavailableException e)
			{
				if (DEBUG) out(e);
			}
		}
		else
		{
			/*	We retrieve a Receiver for the default
				MidiDevice.
			*/
			try
			{
				receiver = MidiSystem.getReceiver();
			}
			catch (MidiUnavailableException e)
			{
				if (DEBUG) { out(e); }
			}
		}
		if (receiver == null)
		{
			out("wasn't able to retrieve Receiver");
			System.exit(1);
		}

		if (DEBUG) out("Receiver: " + receiver);
		/*	Here, we prepare the MIDI messages to send.
			Obviously, one is for turning the key on and
			one for turning it off.
		*/
		ShortMessage	onMessage = null;
		ShortMessage	offMessage = null;
		try
		{
			onMessage = new ShortMessage();
			offMessage = new ShortMessage();
			onMessage.setMessage(ShortMessage.NOTE_ON, nChannel, nKey, nVelocity);
			offMessage.setMessage(ShortMessage.NOTE_OFF, nChannel, nKey, 0);

			if (DEBUG)
			    {
			    out("On Msg: " + onMessage.getStatus() + " " + onMessage.getData1() + " " + onMessage.getData2());
			    out("Off Msg: " + offMessage.getStatus() + " " + offMessage.getData1() + " " + offMessage.getData2());
			}
		}
		catch (InvalidMidiDataException e)
		{
			if (DEBUG) { out(e); }
		}

		/*
		 *	Turn the note on
		 */
		if (DEBUG) out("sending on message...");
		receiver.send(onMessage, -1);
		if (DEBUG) out("...sent");

		/*
		 *	Wait for the specified amount of time
		 *	(the duration of the note).
		 */
		try
		{
			Thread.sleep(nDuration);
		}
		catch (InterruptedException e)
		{
			if (DEBUG) out(e);
		}

		/*
		 *	Turn the note off.
		 */
		if (DEBUG) out("sending off message...");
		receiver.send(offMessage, -1);
		if (DEBUG) out("...sent");

		/*
		 *	Clean up.
		 */
		receiver.close();
		if (outputDevice != null)
		{
			outputDevice.close();
		}
	}



	private static void printUsageAndExit()
	{
		out("MidiNote: usage:");
		out("  java MidiNote [<device name>] <note number> <velocity> <duration>");
		out("    <device name>\toutput to named device");
		System.exit(1);
	}



	private static void out(String strMessage)
	{
		System.out.println(strMessage);
	}



	private static void out(Throwable t)
	{
		t.printStackTrace();
	}
}



/*** MidiNote.java ***/
