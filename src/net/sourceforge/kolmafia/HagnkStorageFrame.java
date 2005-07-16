/**
 * Copyright (c) 2005, KoLmafia development team
 * http://kolmafia.sourceforge.net/
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 *  [1] Redistributions of source code must retain the above copyright
 *      notice, this list of conditions and the following disclaimer.
 *  [2] Redistributions in binary form must reproduce the above copyright
 *      notice, this list of conditions and the following disclaimer in
 *      the documentation and/or other materials provided with the
 *      distribution.
 *  [3] Neither the name "KoLmafia development team" nor the names of
 *      its contributors may be used to endorse or promote products
 *      derived from this software without specific prior written
 *      permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS
 * FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE
 * COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING,
 * BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT
 * LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN
 * ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */

package net.sourceforge.kolmafia;

import java.awt.Dimension;
import java.awt.CardLayout;

import javax.swing.JTextField;
import javax.swing.JTabbedPane;
import javax.swing.JOptionPane;

import net.java.dev.spellcast.utilities.LockableListModel;

/**
 * An extension of <code>KoLFrame</code> which handles all the clan
 * management functionality of Kingdom of Loathing.
 */

public class HagnkStorageFrame extends KoLFrame
{
	private ItemWithdrawPanel items;
	private MeatWithdrawPanel meats;

	public HagnkStorageFrame( KoLmafia client )
	{
		super( client, "KoLmafia: Hagnk, the Secret Dwarf" );

		this.meats = new MeatWithdrawPanel();
		this.items = new ItemWithdrawPanel();

		JTabbedPane tabs = new JTabbedPane();

		tabs.addTab( "Meat", meats );
		tabs.addTab( "Items", items );

		getContentPane().setLayout( new CardLayout( 10, 10 ) );
		getContentPane().add( tabs, "" );
	}

	public void setEnabled( boolean isEnabled )
	{
		if ( items != null )
			items.setEnabled( isEnabled );
		if ( meats != null )
			meats.setEnabled( isEnabled );
	}

	/**
	 * An internal class which represents the panel used for meatss to
	 * the clan coffer.
	 */

	private class MeatWithdrawPanel extends KoLPanel
	{
		private JTextField amountField;

		public MeatWithdrawPanel()
		{
			super( "withdraw meat", "toss dwarf", new Dimension( 80, 20 ), new Dimension( 240, 20 ) );

			amountField = new JTextField();
			VerifiableElement [] elements = new VerifiableElement[1];
			elements[0] = new VerifiableElement( "Amount: ", amountField );
			setContent( elements );
		}

		public void setEnabled( boolean isEnabled )
		{
			super.setEnabled( isEnabled );
			amountField.setEnabled( isEnabled );
		}

		protected void actionConfirmed()
		{	(new RequestThread( new ItemStorageRequest( client, getValue( amountField ), ItemStorageRequest.PULL_MEAT_FROM_STORAGE ) )).start();
		}

		protected void actionCancelled()
		{	JOptionPane.showMessageDialog( null, "Hagnk's actually a gnome.  Tosser." );
		}
	}

	/**
	 * Internal class used to handle everything related to
	 * placing items into the stash.
	 */

	private class ItemWithdrawPanel extends ItemManagePanel
	{
		public ItemWithdrawPanel()
		{
			super( "Inside Storage", "put in bag", "put in closet", client == null ? new LockableListModel() : client.getStorage() );

			if ( client.getStorage().isEmpty() )
				(new RequestThread( new ItemStorageRequest( client ) )).start();

		}

		protected void actionConfirmed()
		{	withdraw( false );
		}

		protected void actionCancelled()
		{	withdraw( true );
		}

		private void withdraw( boolean isCloset )
		{
			Object [] items = elementList.getSelectedValues();
			AdventureResult selection;
			int itemID, quantity;

			try
			{
				for ( int i = 0; i < items.length; ++i )
				{
					selection = (AdventureResult) items[i];
					itemID = selection.getItemID();
					quantity = df.parse( JOptionPane.showInputDialog(
						"Retrieving " + selection.getName() + " from the storage...", String.valueOf( selection.getCount() ) ) ).intValue();

					items[i] = new AdventureResult( itemID, quantity );
				}
			}
			catch ( Exception e )
			{
				// If an exception occurs somewhere along the way
				// then return from the thread.

				return;
			}

			Runnable [] requests = isCloset ? new Runnable[2] : new Runnable[1];
			requests[0] = new ItemStorageRequest( client, ItemStorageRequest.STORAGE_TO_INVENTORY, items );

			if ( isCloset )
				requests[1] = new ItemStorageRequest( client, ItemStorageRequest.INVENTORY_TO_CLOSET, items );

			(new RequestThread( requests )).start();
		}
	}

	public static void main( String [] args )
	{
		Object [] parameters = new Object[1];
		parameters[0] = null;

		(new CreateFrameRunnable( HagnkStorageFrame.class, parameters )).run();
	}
}
