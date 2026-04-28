import { storeToRefs } from "pinia";
import { usePosterGeneratorStore } from "@/stores/posterGenerator";

export function usePosterGenerator() {
  const store = usePosterGeneratorStore();
  const state = storeToRefs(store);

  return {
    store,
    ...state,
  };
}
