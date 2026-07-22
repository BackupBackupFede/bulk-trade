# Bulk Trade — NeoForge 1.21.1

Écoule tout ton stock chez un marchand en un seul shift-clic.

## Ce que fait (et ne fait pas) le mod

Important, car c'est contre-intuitif : **le vanilla enchaîne déjà les échanges** quand tu
shift-cliques le résultat d'un marchand (`AbstractContainerMenu.doClick` rappelle
`quickMoveStack` en boucle). Mais il ne consomme que ce que tu as **posé à la main dans
les 2 cases de paiement** (~2 stacks max), et **ne recharge pas** ces cases depuis le
reste de ton inventaire.

Le mod ajoute **exactement une chose** : recharger les cases de paiement depuis
l'inventaire avant chaque échange. Résultat : un shift-clic vide *tout* ton stock
concerné au lieu de s'arrêter après 2 stacks. C'est un confort (halls de trading,
conversion de ressources en émeraudes), pas une feature manquante.

Implémentation : un seul Mixin (`@Inject` au HEAD de `quickMoveStack`, slot résultat)
qui appelle la méthode vanilla `moveFromInventoryToPaymentSlot`, puis laisse vanilla
faire le trade. Zéro logique de trade dupliquée. Côté serveur, sans dépendance.

| Config (serveur) | Défaut | Effet |
|---|---|---|
| `enabled` | `true` | Active le refill. `false` = comportement vanilla strict. |

## Build & run

Prérequis : **JDK 21**. Wrapper Gradle inclus.

```bash
cd bulk-trade
./gradlew build          # -> build/libs/bulktrade-0.1.0.jar
./gradlew runClient      # client de test avec le mod
```

Le 1er `build` décompile Minecraft (long, mis en cache) ; les suivants sont en secondes.
Sous IntelliJ : « Open » le dossier `bulk-trade`, puis lancer la run config `runClient`.

## Test en jeu

1. Monde Créatif, un villageois avec métier (ou marchand ambulant).
2. Pose du paiement dans les cases (comme d'habitude), sélectionne un trade.
3. **Shift-clic** sur le slot résultat.
4. Attendu : l'échange se répète jusqu'à épuiser ton inventaire du paiement (pas juste
   les 2 cases). Compare `enabled=false` (s'arrête après ~2 stacks) vs `true`.

Le paiement restant dans les cases est rendu à l'inventaire à la fermeture de l'UI
(`MerchantMenu.removed`), donc pas de perte d'items.

## Cas à vérifier

- Trades à **deux ingrédients** (costA + costB) : les deux cases sont rechargées.
- **Marchand ambulant** : même `MerchantMenu`, doit marcher pareil.
- **Composants d'items** (livres enchantés en paiement) : `moveFromInventoryToPaymentSlot`
  filtre via `ItemCost.test`, donc gère les composants ; à confirmer en jeu.

## Prochaines étapes

1. Vérifier CurseForge qu'aucun équivalent NeoForge actif n'existe (angle mort du scan).
2. Tester en jeu, ajuster si besoin.
3. Publier sur Modrinth (NeoForge, 1.21.1), puis envisager un port Fabric.

## Licence

MIT (placeholder — ajouter un `LICENSE` avant publication).
