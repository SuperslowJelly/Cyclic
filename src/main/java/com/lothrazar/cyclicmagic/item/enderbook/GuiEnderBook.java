/*******************************************************************************
 * The MIT License (MIT)
 * 
 * Copyright (C) 2014-2018 Sam Bassett (aka Lothrazar)
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 ******************************************************************************/
package com.lothrazar.cyclicmagic.item.enderbook;

import java.io.IOException;
import java.util.ArrayList;
import org.lwjgl.input.Keyboard;
import com.lothrazar.cyclicmagic.ModCyclic;
import com.lothrazar.cyclicmagic.data.BlockPosDim;
import com.lothrazar.cyclicmagic.data.ITooltipButton;
import com.lothrazar.cyclicmagic.util.UtilChat;
import com.lothrazar.cyclicmagic.util.UtilWorld;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;// http://www.minecraftforge.net/forum/index.php?topic=22378.0
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class GuiEnderBook extends GuiScreen {

  public static final int BACK_BTN_ID = 7777;
  private final EntityPlayer entityPlayer;
  private ItemStack bookStack;
  final int maxNameLen = 20;

  public GuiEnderBook(EntityPlayer entityPlayer, ItemStack book) {
    this.entityPlayer = entityPlayer;
    bookStack = book;
  }

  public static int buttonIdNew;
  GuiButton buttonNew;
  GuiTextField txtNew;
  final int DELETE_OFFSET = 1000;
  private ButtonWaypointTeleport btnBack;

  @Override
  public void initGui() {
    Keyboard.enableRepeatEvents(true);
    // great tips here
    // http://www.minecraftforge.net/forum/index.php?topic=29945.0
    if (bookStack.hasTagCompound() == false) {
      bookStack.setTagCompound(new NBTTagCompound());
    }
    int buttonID = 0, w = 70, h = 20, ypad = 1, delete_w = 20, rowpad = 8;
    buttonIdNew = buttonID;
    buttonID++;
    buttonNew = new ButtonWaypointNew(buttonIdNew, this.width / 2 - w, // x
        20, // y
        w, h, buttonIdNew);
    addButton(buttonNew);
    ButtonClose buttonClose = new ButtonClose(9999, this.width / 2 - w - 50, 20);
    addButton(buttonClose);
    txtNew = new GuiTextField(buttonID++, this.fontRenderer, buttonNew.x + buttonNew.width + 10, buttonNew.y, w, h);
    txtNew.setMaxStringLength(maxNameLen);
    // default to the current biome
    txtNew.setText(entityPlayer.getEntityWorld().getBiome(entityPlayer.getPosition()).getBiomeName());
    txtNew.setFocused(true);
    btnBack = new ButtonWaypointTeleport(BACK_BTN_ID,
        txtNew.x + txtNew.width + 8,
        buttonClose.y, w / 2, h,
        UtilChat.lang("gui.enderbook.back"), BACK_BTN_ID);
    this.addButton(btnBack);
    BlockPosDim location = ItemEnderBook.getBackLocation(bookStack);
    btnBack.enabled = location != null;
    if (bookStack != null && ItemEnderBook.getLocations(bookStack).size() >= ItemEnderBook.maximumSaved) {
      buttonNew.enabled = false;
    }
    ButtonWaypointTeleport btn;
    GuiButton del;
    BlockPosDim loc;
    String buttonText;
    int yStart = 45;
    int xStart = (this.width / 10);
    int x = xStart;
    int y = yStart;
    ArrayList<BlockPosDim> list = ItemEnderBook.getLocations(bookStack);
    for (int i = 0; i < list.size(); i++) {
      loc = list.get(i);
      buttonText = (loc.getDisplay() == null) ? UtilChat.lang("gui.enderbook.go") : loc.getDisplay();
      if (i % ItemEnderBook.BTNS_PER_COLUMN == 0) // do we start a new row?
      {
        x += w + delete_w + rowpad;
        y = yStart;
      }
      else {
        y += h + ypad;
      }
      btn = new ButtonWaypointTeleport(buttonID++, x, y, w, h, buttonText, loc.id);
      BlockPos toPos = list.get(i).toBlockPos();
      int distance = (int) UtilWorld.distanceBetweenHorizontal(toPos, entityPlayer.getPosition());
      if (loc.getDimension() == this.entityPlayer.dimension) {
        btn.enabled = true;
        btn.addTooltipLine(list.get(i).coordsDisplay());
        if (distance > 0) {
          btn.addTooltipLine(UtilChat.lang("button.waypoint.distance") + " " + distance);
        }
        int cost = ItemEnderBook.getExpCostPerTeleport(entityPlayer, bookStack, loc.id);
        if (cost > 0) {
          btn.addTooltipLine(UtilChat.lang("button.waypoint.cost") + " " + cost);
        }
      }
      else {
        btn.enabled = false;
        btn.addTooltipLine(UtilChat.lang("button.waypoint.dimension") + " " + loc.getDimension());
      }
      buttonList.add(btn);
      del = new ButtonWaypointDelete(buttonID++, x - delete_w - 2, y, delete_w, h, "X", loc.id);
      buttonList.add(del);
    }
  }

  @Override
  public void onGuiClosed() {
    Keyboard.enableRepeatEvents(false);
  }

  @Override
  public void drawScreen(int x, int y, float par3) {
    drawDefaultBackground();
    drawCenteredString(fontRenderer, UtilChat.lang("gui.enderbook.title"), width / 2, 6, 16777215);
    if (btnBack != null) {
      btnBack.setTooltipLine(UtilChat.lang("gui.enderbook.back.tooltip") + "[" + ItemEnderBook.BACK_TICKS / 20 + "]");
    }
    // http://www.minecraftforge.net/forum/index.php?topic=22378.0
    if (txtNew != null) {
      txtNew.drawTextBox();
    }
    super.drawScreen(x, y, par3);
    for (int i = 0; i < buttonList.size(); i++) {
      if (buttonList.get(i).isMouseOver() && buttonList.get(i) instanceof ITooltipButton) {
        ITooltipButton btn = (ITooltipButton) buttonList.get(i);
        drawHoveringText(btn.getTooltips(), x, y, fontRenderer);
      }
    }
  }

  @Override
  protected void actionPerformed(GuiButton btn) {
    if (btn.id == buttonIdNew) {
      ModCyclic.network.sendToServer(new PacketNewButton(txtNew.getText()));
    }
    else if (btn instanceof ButtonWaypointDelete) {
      ModCyclic.network.sendToServer(new PacketDeleteWaypoint(((ButtonWaypointDelete) btn).getSlot()));
    }
    else if (btn instanceof ButtonWaypointTeleport) {
      // moved to btn class
    }
    this.entityPlayer.closeScreen();
  }

  @Override
  public boolean doesGuiPauseGame() {
    return false;
  }

  // http://www.minecraftforge.net/forum/index.php?topic=22378.0
  // below is all the stuff that makes the text box NOT broken
  @Override
  public void updateScreen() {
    super.updateScreen();
    if (txtNew != null) {
      txtNew.updateCursorCounter();
    }
  }

  @Override
  protected void keyTyped(char par1, int par2) throws IOException {
    super.keyTyped(par1, par2);
    if (txtNew != null) {
      txtNew.textboxKeyTyped(par1, par2);
    }
  }

  @Override
  protected void mouseClicked(int x, int y, int btn) throws IOException {
    super.mouseClicked(x, y, btn);
    if (txtNew != null) {
      txtNew.mouseClicked(x, y, btn);
    }
  }
  // ok end of textbox fixing stuff
}
