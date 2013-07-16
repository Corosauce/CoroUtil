package CoroAI.componentAI;

public interface IInvOutsourced {
	public void syncEntToOutsourcedInventory(AIFakePlayer parAIFakePlayer);
	public void syncOutsourcedToEntInventory(AIFakePlayer parAIFakePlayer);
	public boolean canAttack();
}
