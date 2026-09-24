# Biology + Ecology Contract — team/biology-ecology-v1

This branch does not own rendering, God, personality, updater, world physics, or Haru decision authority. All outputs below are read-only interpretations of WORLD STATE.

## Haru biology -> Visual
PRODUCER: BIOLOGY/ECOLOGY  
CONSUMER: VISUAL (read-only), CHARACTER perception may read existing body state only  
FIELD: BiologyVisualOutput.breathingIntensity | UNIT: normalized | RANGE: 0..1 | MEANING: ventilation demand + air effort + oxygen debt | DEFAULT: 0 | SAVE/PERSISTENCE: derived, not saved  
FIELD: postureLoad | UNIT: normalized | RANGE: 0..1 | MEANING: guarding/fatigue/balance load, never body deformation | DEFAULT: 0 | SAVE/PERSISTENCE: derived  
FIELD: localized pain channels + dominantPainRegion | UNIT: normalized | RANGE: 0..1 | MEANING: regional nociceptive load | DEFAULT: 0/null | SAVE/PERSISTENCE: source LocalizedPainState is saved  
FIELD: tremor | UNIT: normalized | RANGE: 0..1 | MEANING: shiver + motor correction + fatigue | DEFAULT: 0 | SAVE/PERSISTENCE: derived  
FIELD: fatigue | UNIT: normalized | RANGE: 0..1 | MEANING: muscle fatigue + low energy + sleep pressure + oxygen debt | DEFAULT: 0 | SAVE/PERSISTENCE: derived  
FIELD: gaitChange | UNIT: normalized | RANGE: 0..1 | MEANING: visible gait alteration demanded by pain/fatigue/balance | DEFAULT: 0 | SAVE/PERSISTENCE: derived  
FIELD: gaitAsymmetry | UNIT: signed normalized | RANGE: -1..1 | MEANING: right-leg pain minus left-leg pain; visual may alter gait timing/load, never sprite proportions | DEFAULT: 0 | SAVE/PERSISTENCE: derived  
FIELD: thermalDiscomfort | UNIT: signed normalized | RANGE: -1..1 | MEANING: cold negative, heat positive | DEFAULT: 0 | SAVE/PERSISTENCE: derived  
FIELD: recoveryState/recoveryLoad | UNIT: enum + normalized | RANGE: BASELINE/STRAINED/ACTIVE_RECOVERY/REST_RECOVERY/SLEEP_RECOVERY + 0..1 | MEANING: recovery presentation state | DEFAULT: BASELINE/0 | SAVE/PERSISTENCE: derived

## Population ecology
PRODUCER: BIOLOGY/ECOLOGY  
CONSUMER: ECOLOGY movement/resource logic; VISUAL may use relative abundance to choose representative count only  
FIELD: LivingWorldState.populations[species@area].relativeAbundance | UNIT: relative local abundance | RANGE: 0..1 | MEANING: population field, not literal spawned-object count | DEFAULT: habitat-derived | SAVE/PERSISTENCE: saved in livingWorld.populations  
FIELD: carryingCapacity | UNIT: normalized | RANGE: 0..1 | MEANING: resource/habitat/weather-supported capacity | DEFAULT: habitat-derived | SAVE/PERSISTENCE: saved  
FIELD: birthPressure/recoveryPressure/mortalityPressure/competitionPressure/migrationPressure/resourcePressure/weatherPressure | UNIT: normalized | RANGE: 0..1 | MEANING: causal population pressures | DEFAULT: bounded defaults | SAVE/PERSISTENCE: saved  
FIELD: seasonalInfluence | UNIT: signed normalized | RANGE: -1..1 | MEANING: reserved input when WORLD/ENVIRONMENT publishes an authored season signal; BIOLOGY does not invent seasons | DEFAULT: 0 | SAVE/PERSISTENCE: saved

Invariant: visual consumers must never write these states back or invent body/ecology events. Individual visible creatures remain representatives; population state does not spawn thousands of objects.


## Evolutionary species reality — locked for V1.0.1
- System Reality contains exactly **500 terminal base species**.
- All 500 descend through 10 clades / 50 intermediate lineages from the single hidden root ancestor `first_lumen_ancestor`.
- Hybrids, mutations, color morphs and individual variants are **not** counted toward the 500.
- Every base species must have a unique trait fingerprint; collision is a release-blocking invariant.
- Runtime population state is sparse. The catalog may know 500 species without materializing 500 x every area or spawning hundreds of visible objects.
- Existing authored creatures remain representative individuals, not literal population counts.
- The full evolutionary catalog is `GOD_SYSTEM_ONLY`. Haru can infer ecology from lived observation, and can learn lineage only if Thần chooses to reveal/teach it through Haru's autonomous learning pipeline.
