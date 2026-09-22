# Character + God Contract — team/character-god-v1

This branch owns Haru interpretation/decision/memory/knowledge and the bounded God interface. It does not own rendering, physics, atmosphere evolution or biology/ecology implementation.

## God world-condition proposal -> World/Physics
PRODUCER: CHARACTER/GOD  
CONSUMER: WORLD/PHYSICS  
FIELD: CharacterGodState.worldConditions[id].condition | UNIT: enum | RANGE: WEATHER / WIND / HUMIDITY_MIST / TEMPERATURE / LIGHT / ATMOSPHERE_PERTURBATION | MEANING: requested environmental condition class, proposal only until consumer applies | DEFAULT: none | SAVE/PERSISTENCE: saved in characterGod.worldConditions  
FIELD: desiredValue | UNIT: semantic enum/string | RANGE: bounded by consumer for the condition class | MEANING: requested qualitative target such as BREEZE or LIGHT_MIST; never a direct physics write | DEFAULT: empty | SAVE/PERSISTENCE: saved  
FIELD: scopeType + scopeTarget | UNIT: enum + authored area id | RANGE: GLOBAL or AREA | MEANING: spatial bound of proposal | DEFAULT: GLOBAL + empty target | SAVE/PERSISTENCE: saved  
FIELD: intensity | UNIT: normalized | RANGE: 0..1 | MEANING: requested bounded strength, not a raw engine force | DEFAULT: bounded input | SAVE/PERSISTENCE: saved  
FIELD: durationMinutes | UNIT: minutes | RANGE: 1..360 | MEANING: maximum requested duration | DEFAULT: 30 | SAVE/PERSISTENCE: saved  
FIELD: provenanceLayer/sourceRef/sourceConfidence | UNIT: enum/string/normalized | RANGE: sourceConfidence -1 unknown or 0..1 | MEANING: why the proposal exists; REAL_REFERENCE requires real source metadata | DEFAULT: GOD_PROPOSAL / god:proposal / unknown | SAVE/PERSISTENCE: saved  
FIELD: rollbackPolicy/status/consumerToken | UNIT: enum/string | RANGE: RESTORE_PREVIOUS_BASELINE or DISSIPATE_TO_BASELINE; PROPOSED/APPLIED/ROLLBACK_REQUESTED/ROLLED_BACK | MEANING: consumer-owned application/rollback lifecycle; markApplied is idempotent and rejects double apply | DEFAULT: PROPOSED | SAVE/PERSISTENCE: saved  

Invariant: God proposes. World/Physics decides how/if to realize new condition types. The legacy ATMOSPHERE_PERTURBATION adapter remains for compatibility and writes only the pre-existing bounded AtmospherePerturbation input.

## Population ecology -> God observation
PRODUCER: BIOLOGY/ECOLOGY  
CONSUMER: CHARACTER/GOD (read-only)  
FIELD: LivingWorldState.populations[species@area].relativeAbundance | UNIT: relative local abundance | RANGE: 0..1 | MEANING: qualitative population observation used by God world summary, never literal spawn count | DEFAULT: population producer unavailable until Biology branch is integrated | SAVE/PERSISTENCE: owned/saved by BIOLOGY/ECOLOGY  
FIELD: carryingCapacity + causal pressures | UNIT: normalized | RANGE: 0..1 (seasonalInfluence -1..1) | MEANING: optional diagnostic context; CHARACTER/GOD must never write it | DEFAULT: producer-defined | SAVE/PERSISTENCE: owned by BIOLOGY/ECOLOGY  

Current branch reads this contract without modifying Biology implementation. If the producer field is absent on the current integration base, God reports population data as unavailable rather than inventing it.

## Haru/God autonomy invariant
God lesson objects are external opportunities. Only Haru's shared causal pipeline can create Haru lesson memory/knowledge evidence. God cannot set Haru emotion, personality, relationship, intention, decision, memory, belief or skill. Skill remains outcome/practice gated.
