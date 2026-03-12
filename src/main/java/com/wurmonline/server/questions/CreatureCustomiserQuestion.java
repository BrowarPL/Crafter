package com.wurmonline.server.questions;

import com.wurmonline.server.creatures.Creature;
import mod.wurmunlimited.npcs.FaceSetter;
import mod.wurmunlimited.npcs.ModelSetter;
import java.util.Properties;

public class CreatureCustomiserQuestion extends Question {
    public CreatureCustomiserQuestion(Creature responder, Creature target, FaceSetter fs, ModelSetter ms, ModelOption[] options) {
        super(responder, "Appearance", "Customise Appearance", 1, target.getWurmId());
    }

    @Override
    public void answer(Properties properties) {}

    @Override
    public void sendQuestion() {
        getResponder().getCommunicator().sendNormalServerMessage("Opcja dostosowywania wyglądu została wyłączona.");
    }
}